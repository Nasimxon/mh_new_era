/*
 * Copyright 2014 Toxic Bakery
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jim.finansia.intropage;

import android.support.v4.view.ViewPager;
import android.view.View;


public class ZoomOutTranformer  extends BaseTransformer {

        public ZoomOutTranformer() {
        }

        protected void onTransform(View view, float position) {
                float scale = 1.0F + Math.abs(position);
                view.setScaleX(scale);
                view.setScaleY(scale);
                view.setPivotX((float)view.getWidth() * 0.5F);
                view.setPivotY((float)view.getHeight() * 0.5F);
                view.setAlpha(position >= -1.0F && position <= 1.0F?1.0F - (scale - 1.0F):0.0F);
                if(position == -1.0F) {
                        view.setTranslationX((float)(view.getWidth() * -1));
                }

        }
}
