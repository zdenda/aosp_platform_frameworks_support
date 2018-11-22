/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.remotecallback;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.RestrictTo;

/**
 * Generates a {@link RemoteCallback} when a RemoteCallback is being triggered, should only
 * be used in the context on {@link CallbackReceiver#createRemoteCallback}.
 *
 * @param <T> Should be specified as the root class (e.g. class X extends
 *           CallbackReceiver\<X>)
 *
 * @hide
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
public interface CallbackBase<T> {

    /**
     * Generates a {@link RemoteCallback} when a RemoteCallback is being triggered, should only
     * be used in the context on {@link CallbackReceiver#createRemoteCallback}.
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    RemoteCallback toRemoteCallback(Class<T> cls, Context context, String authority, Bundle args,
            String method);
}