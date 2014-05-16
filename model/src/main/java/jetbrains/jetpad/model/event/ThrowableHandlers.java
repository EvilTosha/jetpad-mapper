/*
 * Copyright 2012-2014 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.model.event;


import jetbrains.jetpad.base.Registration;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class ThrowableHandlers {
  //we can use ThreadLocal here because of our own emulation at jetbrains.jetpad.model.jre.java.lang.ThreadLocal
  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static ThreadLocal<Boolean> ourForceProduction = new ThreadLocal<Boolean>() {
    @Override
    protected Boolean initialValue() {
      return false;
    }
  };

  @SuppressWarnings("NonJREEmulationClassesInClientCode")
  private static ThreadLocal<SimpleEventSource<Throwable>> ourHandlers = new ThreadLocal<SimpleEventSource<Throwable>>() {
    @Override
    protected SimpleEventSource<Throwable> initialValue() {
      return new SimpleEventSource<>();
    }
  };

  //new handler can't throw anything because it will lead to StackOverflowError!
  public static Registration addHandler(EventHandler<? super Throwable> handler) {
    return ourHandlers.get().addHandler(handler);
  }

  public static void asInProduction(Runnable r) {
    if (ourForceProduction.get()) {
      throw new IllegalStateException();
    }
    ourForceProduction.set(true);
    PrintStream defaultErr = System.err;
    OutputStream out = new OutputStream() {
      public void write(int b) throws IOException {
      }
    };
    System.setErr(new PrintStream(out));
    try {
      r.run();
    } finally {
      System.setErr(defaultErr);
      ourForceProduction.set(false);
    }
  }

  public static void handle(Throwable t) {
    if (isInUnitTests(t)) {
      throw new RuntimeException(t);
    }
    t.printStackTrace();
    ourHandlers.get().fire(t);
  }

  private static boolean isInUnitTests(Throwable t) {
    if (ourForceProduction.get()) {
      return false;
    }
    for (StackTraceElement e : t.getStackTrace()) {
      if (e.getClassName().startsWith("org.junit.runners")) {
        return true;
      }
    }
    return false;
  }
}