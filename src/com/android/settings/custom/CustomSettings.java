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

import android.content.ContentResolver;
import android.content.Context;
import android.text.TextUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.UserHandle;

import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.util.custom.Utils;

import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settingslib.search.SearchIndexable;

import org.eu.materium.support.preferences.CustomSystemSeekBarPreference;

import lineageos.hardware.LineageHardwareManager;
import lineageos.providers.LineageSettings;

@SearchIndexable
public class CustomSettings extends DashboardFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "CustomSettings";

    private static final String KEY_COMBINED_ICONS = "combined_status_bar_signal_icons";
    private static final String KEY_FORCE_FULL_SCREEN = "display_cutout_force_fullscreen_settings";
    private static final String KEY_GAMES_SPOOF = "use_games_spoof";
    private static final String KEY_VOLTE_ICON_STYLE = "volte_icon_style";
    private static final String KEY_VOWIFI_ICON_STYLE = "vowifi_icon_style";
    private static final String KEY_VOLTE_VOWIFI_OVERRIDE = "volte_vowifi_override";
    private static final String KEY_SHOW_ROAMING = "roaming_indicator_icon";
    private static final String KEY_SHOW_FOURG = "show_fourg_icon";
    private static final String KEY_SHOW_DATA_DISABLED = "data_disabled_icon";
    private static final String NAVBAR_VISIBILITY = "navbar_visibility";
    private static final String HWKEYS_DISABLED = "hardware_keys_disable";

    private static final String SYS_GAMES_SPOOF = "persist.sys.pixelprops.games";

    private SwitchPreference mCombinedIcons;
    private SwitchPreference mShowCutoutForce;
    private SwitchPreference mGamesSpoof;
    private CustomSystemSeekBarPreference mVolteIconStyle;
    private CustomSystemSeekBarPreference mVowifiIconStyle;
    private SwitchPreference mOverride;
    private SwitchPreference mShowRoaming;
    private SwitchPreference mShowFourg;
    private SwitchPreference mDataDisabled;
    private SwitchPreference mNavbarVisibility;
    private SwitchPreference mHardwareKeysDisable;

    private boolean mIsNavSwitchingMode = false;
    private Handler mHandler;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final PreferenceScreen prefScreen = getPreferenceScreen();
	mCombinedIcons = (SwitchPreference) findPreference(KEY_COMBINED_ICONS);
	mVolteIconStyle = (CustomSystemSeekBarPreference) findPreference(KEY_VOLTE_ICON_STYLE);
	mVowifiIconStyle = (CustomSystemSeekBarPreference) findPreference(KEY_VOWIFI_ICON_STYLE);
	mOverride = (SwitchPreference) findPreference(KEY_VOLTE_VOWIFI_OVERRIDE);
	mShowRoaming = (SwitchPreference) findPreference(KEY_SHOW_ROAMING);
	mShowFourg = (SwitchPreference) findPreference(KEY_SHOW_FOURG);
	mDataDisabled = (SwitchPreference) findPreference(KEY_SHOW_DATA_DISABLED);
	mHardwareKeysDisable = (SwitchPreference) findPreference(HWKEYS_DISABLED);

	if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            prefScreen.removePreference(mCombinedIcons);
	    prefScreen.removePreference(mVolteIconStyle);
	    prefScreen.removePreference(mVowifiIconStyle);
	    prefScreen.removePreference(mOverride);
	    prefScreen.removePreference(mShowRoaming);
	    prefScreen.removePreference(mShowFourg);
	    prefScreen.removePreference(mDataDisabled);
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

	ContentResolver resolver = getActivity().getContentResolver();
        mHandler = new Handler();

        mNavbarVisibility = (SwitchPreference) findPreference(NAVBAR_VISIBILITY);

        boolean showing = LineageSettings.System.getIntForUser(resolver,
                LineageSettings.System.FORCE_SHOW_NAVBAR,
                Utils.hasNavbarByDefault(getActivity()) ? 1 : 0, UserHandle.USER_CURRENT) != 0;
        mNavbarVisibility.setChecked(showing);
        mNavbarVisibility.setOnPreferenceChangeListener(this);

	if (isKeyDisablerSupported(getActivity())) {
            mHardwareKeysDisable.setOnPreferenceChangeListener(this);
        } else {
            prefScreen.removePreference(mHardwareKeysDisable);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	ContentResolver resolver = getActivity().getContentResolver();

	if (preference == mHardwareKeysDisable) {
            // do nothing for now
            return true;
        }

        if (preference == mNavbarVisibility) {
            if (mIsNavSwitchingMode) {
                return false;
            }
            mIsNavSwitchingMode = true;
            boolean showing = ((Boolean)newValue);
            LineageSettings.System.putIntForUser(resolver, LineageSettings.System.FORCE_SHOW_NAVBAR,
                    showing ? 1 : 0, UserHandle.USER_CURRENT);
            mNavbarVisibility.setChecked(showing);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mIsNavSwitchingMode = false;
                }
            }, 1500);

            return true;
        }

        if (preference == mGamesSpoof) {
            boolean value = (Boolean) newValue;
            SystemProperties.set(SYS_GAMES_SPOOF, value ? "true" : "false");
            return true;
        }
	return true;
    }

    private static boolean isKeyDisablerSupported(Context context) {
        final LineageHardwareManager hardware = LineageHardwareManager.getInstance(context);
        return hardware.isSupported(LineageHardwareManager.FEATURE_KEY_DISABLE);
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
			keys.add(KEY_VOLTE_ICON_STYLE);
			keys.add(KEY_VOWIFI_ICON_STYLE);
			keys.add(KEY_VOLTE_VOWIFI_OVERRIDE);
			keys.add(KEY_SHOW_ROAMING);
			keys.add(KEY_SHOW_FOURG);
			keys.add(KEY_SHOW_DATA_DISABLED);
                    }

	            final String displayCutout = context.getResources().getString(com.android.internal.R.string.config_mainBuiltInDisplayCutout);
                    if (TextUtils.isEmpty(displayCutout)) {
                        keys.add(KEY_FORCE_FULL_SCREEN);
                    }

		    if (!isKeyDisablerSupported(context))
                        keys.add(HWKEYS_DISABLED);

                    return keys;
                }
            };
}
