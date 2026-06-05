package org.d1scw0rld.bookbag;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BaseObservable<LISTENER_CLASS>
{
   private final Set<LISTENER_CLASS> listeners = Collections.newSetFromMap(new ConcurrentHashMap<>(1));

   public final void registerListener(LISTENER_CLASS listener)
   {
      listeners.add(listener);
   }

   public final void unregisterListener(LISTENER_CLASS listener)
   {
      listeners.remove(listener);
   }

   protected Set<LISTENER_CLASS> getListeners()
   {
      return Collections.unmodifiableSet(listeners);
   }
}
