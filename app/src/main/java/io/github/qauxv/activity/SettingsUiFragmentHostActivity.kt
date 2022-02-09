/*
 * QAuxiliary - An Xposed module for QQ/TIM
 * Copyright (C) 2019-2022 qwq233@qwq2333.top
 * https://github.com/cinit/QAuxiliary
 *
 * This software is non-free but opensource software: you can redistribute it
 * and/or modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either
 * version 3 of the License, or any later version and our eula as published
 * by QAuxiliary contributors.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * and eula along with this software.  If not, see
 * <https://www.gnu.org/licenses/>
 * <https://github.com/cinit/QAuxiliary/blob/master/LICENSE.md>.
 */

package io.github.qauxv.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import io.github.qauxv.R
import io.github.qauxv.base.IUiItemAgentProvider
import io.github.qauxv.fragment.BaseSettingFragment
import io.github.qauxv.fragment.SettingsMainFragment
import io.github.qauxv.util.UiThread

class SettingsUiFragmentHostActivity : AppCompatTransferActivity() {

    private val mFragmentStack = ArrayList<BaseSettingFragment>(4)
    private var mTopVisibleFragment: BaseSettingFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_Def)
        // TODO 2022-01-31: update day night color according to the host app
        // we don't want the Fragment to be recreated
        super.onCreate(null)
        setContentView(R.layout.activity_settings_ui_host)
        val intent = intent
        // check if we are requested to show a specific fragment
        val fragmentName: String? = intent.getStringExtra(TARGET_FRAGMENT_KEY)
        val startupFragment: BaseSettingFragment = if (fragmentName != null) {
            val clazz = Class.forName(fragmentName)
            val fragment = clazz.newInstance() as BaseSettingFragment
            val args: Bundle? = intent.getBundleExtra(TARGET_FRAGMENT_ARGS_KEY)
            if (args != null) {
                fragment.arguments = args
            }
            fragment
        } else {
            // otherwise, show the default fragment
            SettingsMainFragment.newInstance(arrayOf())
        }
        // add the fragment to the stack
        presentFragment(startupFragment)
    }

    /**
     * Navigate to the specified UI item.
     */
    @UiThread
    fun navigateToFunctionUiItemEntry(targetItem: IUiItemAgentProvider) {
        TODO("not implemented")
    }

    fun presentFragment(fragment: BaseSettingFragment) {
        rtlAddFragmentToTop(fragment)
    }

    fun finishFragment(fragment: BaseSettingFragment) {
        rtlRemoveFragment(fragment)
    }

    fun popCurrentFragment() {
        val fragment = mFragmentStack.lastOrNull()
        if (fragment != null) {
            rtlRemoveFragment(fragment)
        } else {
            finish()
        }
    }

    override fun onBackPressed() {
        val consumed = mTopVisibleFragment?.doOnBackPressed() ?: false
        if (!consumed) {
            popCurrentFragment()
        }
    }

    private fun rtlAddFragmentToTop(fragment: BaseSettingFragment) {
        if (mFragmentStack.isEmpty()) {
            // first fragment
            supportFragmentManager.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit()
            mTopVisibleFragment = fragment
            mFragmentStack.add(fragment)
            title = fragment.title
        } else {
            // replace the top fragment
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.commit()
            mTopVisibleFragment = fragment
            mFragmentStack.add(fragment)
            title = fragment.title
        }
    }

    private fun rtlRemoveFragment(fragment: BaseSettingFragment) {
        // remove
        supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
        mFragmentStack.remove(fragment)
        // check if we need to show the previous fragment
        if (fragment == mTopVisibleFragment) {
            mTopVisibleFragment = mFragmentStack.lastOrNull()
            if (mTopVisibleFragment == null) {
                finish()
            } else {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, mTopVisibleFragment!!)
                        .commit()
                title = mTopVisibleFragment!!.title
            }
        }
    }

    companion object {
        const val TARGET_FRAGMENT_KEY: String = "SettingsUiFragmentHostActivity.TARGET_FRAGMENT_KEY"
        const val TARGET_FRAGMENT_ARGS_KEY: String = "SettingsUiFragmentHostActivity.TARGET_FRAGMENT_ARGS_KEY"

        @JvmStatic
        fun startActivityForFragment(context: Context,
                                     fragmentClass: Class<out BaseSettingFragment>,
                                     args: Bundle? = null) {
            context.startActivity(createStartActivityForFragmentIntent(context, fragmentClass, args))
        }

        @JvmStatic
        fun createStartActivityForFragmentIntent(context: Context,
                                                 fragmentClass: Class<out BaseSettingFragment>,
                                                 args: Bundle? = null): Intent {
            val intent = Intent(context, SettingsUiFragmentHostActivity::class.java)
            intent.putExtra(TARGET_FRAGMENT_KEY, fragmentClass.name)
            intent.putExtra(TARGET_FRAGMENT_ARGS_KEY, args)
            return intent
        }
    }
}
