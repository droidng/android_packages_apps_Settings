/*
 * Copyright (C) 2022 Project Materium
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

package com.android.settings.custom;

import java.util.List;

import android.content.Context;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class CustomSettings extends DashboardFragment {

    private static final String TAG = "CustomSettings";

    private static final String KEY_COMBINED_ICONS = "combined_status_bar_signal_icons";

    private SwitchPreference mCombinedIcons;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen prefScreen = getPreferenceScreen();
	mCombinedIcons = (SwitchPreference) findPreference(KEY_COMBINED_ICONS);

	if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mCombinedIcons);
	}
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.CUSTOM_SETTINGS;
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.materium_top_level;
    }

    @Override
    protected String getLogTag() {
        return TAG;
    }

    /**
     * For Search.
     */
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider(R.xml.materium_top_level) {
                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    List<String> keys = super.getNonIndexableKeys(context);
                    if (!TelephonyUtils.isVoiceCapable(context)) {
                        keys.add(KEY_COMBINED_ICONS);
                    }

                    return keys;
                }
            };
}
