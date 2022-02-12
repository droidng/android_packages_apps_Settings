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
import android.text.TextUtils;
import android.os.Bundle;
import android.os.SystemProperties;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

@SearchIndexable
public class CustomSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "CustomSettings";

    private static final String KEY_COMBINED_ICONS = "combined_status_bar_signal_icons";
    private static final String KEY_FORCE_FULL_SCREEN = "display_cutout_force_fullscreen_settings";
    private static final String KEY_GAMES_SPOOF = "use_games_spoof";

    private static final String SYS_GAMES_SPOOF = "persist.sys.pixelprops.games";

    private SwitchPreference mCombinedIcons;
    private SwitchPreference mShowCutoutForce;
    private SwitchPreference mGamesSpoof;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen prefScreen = getPreferenceScreen();
	mCombinedIcons = (SwitchPreference) findPreference(KEY_COMBINED_ICONS);

	if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mCombinedIcons);
	}
        Context mContext = getActivity().getApplicationContext();

	final String displayCutout =
            mContext.getResources().getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout);

        if (TextUtils.isEmpty(displayCutout)) {
            mShowCutoutForce = (SwitchPreference) findPreference(KEY_FORCE_FULL_SCREEN);
            prefScreen.removePreference(mShowCutoutForce);
        }

	mGamesSpoof = (SwitchPreference) prefScreen.findPreference(KEY_GAMES_SPOOF);
        mGamesSpoof.setChecked(SystemProperties.getBoolean(SYS_GAMES_SPOOF, false));
        mGamesSpoof.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mGamesSpoof) {
            boolean value = (Boolean) newValue;
            SystemProperties.set(SYS_GAMES_SPOOF, value ? "true" : "false");
            return true;
        }
	return true;
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

	            final String displayCutout = context.getResources().getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout);
                    if (TextUtils.isEmpty(displayCutout)) {
                        keys.add(KEY_FORCE_FULL_SCREEN);
                    }

                    return keys;
                }
            };
}
