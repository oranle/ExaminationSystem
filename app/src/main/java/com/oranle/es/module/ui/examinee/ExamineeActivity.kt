package com.oranle.es.module.ui.examinee

import android.os.Bundle
import android.view.View
import com.oranle.es.R
import com.oranle.es.databinding.ActivityExamineeBinding
import com.oranle.es.module.base.BaseActivity
import com.oranle.es.module.base.BaseFragment
import com.oranle.es.module.ui.administrator.fragment.ReportFragment
import com.oranle.es.module.ui.examinee.fragment.ObjectiveTestSelectFragment
import com.oranle.es.module.ui.examinee.fragment.SubjectiveTestSelectFragment
import com.oranle.es.module.ui.senior.fragment.ModifyPwdFragment

/**
 *  测评人页面
 */
class ExamineeActivity : BaseActivity<ActivityExamineeBinding>() {

    private val subjectiveTestSelectFragment = SubjectiveTestSelectFragment()
    private val objectiveTestSelectFragment = ObjectiveTestSelectFragment()
    private val reportFragment = ReportFragment.newInstance()
    private val modifyPwdFragment = ModifyPwdFragment()

    override val layoutId: Int
        get() = R.layout.activity_examinee

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
    }

    private fun initView() {
        showSubSelectFragment(null)
    }

    fun showSubSelectFragment(v: View?) {
        showFragment(subjectiveTestSelectFragment)
    }

    fun showObjSelectFragment(v: View) {
        showFragment(objectiveTestSelectFragment)
    }

    fun showReportFragment(v: View) {
        showFragment(reportFragment)
    }

    fun showModifyInfo(v: View) {
        showFragment(modifyPwdFragment)
    }

    fun onFinish(v: View) {
        finish()
    }

    private fun showFragment(fragment: BaseFragment<*>) {
        try {
            val ft =
                supportFragmentManager!!.beginTransaction()
            ft.replace(R.id.frameLayout, fragment)
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


}