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
package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.event.Registration;

public abstract class SimpleCollectionProperty<ItemT, ValueT> extends BaseDerivedProperty<ValueT> {
  private ObservableCollection<ItemT> myCollection;
  private Registration myRegistration;

  protected SimpleCollectionProperty(ObservableCollection<ItemT> collection, ValueT initialValue) {
    super(initialValue);
    myCollection = collection;
  }

  @Override
  protected void doAddListeners() {
    myRegistration = myCollection.addListener(new CollectionListener<ItemT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<ItemT> event) {
        somethingChanged();
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<ItemT> event) {
        somethingChanged();
      }
    });
  }

  @Override
  protected void doRemoveListeners() {
    myRegistration.remove();
  }
}