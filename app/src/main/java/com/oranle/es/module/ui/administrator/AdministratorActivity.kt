package com.oranle.es.module.ui.administrator

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.oranle.es.R
import com.oranle.es.data.entity.Assessment
import com.oranle.es.data.entity.ClassEntity
import com.oranle.es.data.sp.SpUtil
import com.oranle.es.databinding.ActivityAdminBinding
import com.oranle.es.module.base.BaseActivity
import com.oranle.es.module.base.BaseFragment
import com.oranle.es.module.ui.administrator.fragment.*
import com.oranle.es.module.ui.administrator.fragment.ReportFragment
import com.oranle.es.module.ui.senior.fragment.ModifyPwdFragment
import timber.log.Timber

class AdministratorActivity : BaseActivity<ActivityAdminBinding>() {

    var fragList = mutableListOf<BaseFragment<*>>()

    override val layoutId: Int
        get() = R.layout.activity_admin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("get intent extra %s", intent.extras!!["user"])
        initView()
    }

    @SuppressLint("SetTextI18n")
    private fun initView() {
        dataBinding.apply {
            val currentUser = SpUtil.instance.getCurrentUser()
            currentUser?.apply {
                userInfo.text = "${currentUser!!.getRoleStr()} ${currentUser.alias}"
            }
        }

        fragList.add(LoginManagerFragment())
        fragList.add(ReportFragment.newInstance(isShowAll = true))
        fragList.add(ManualInputFragment())
        fragList.add(GroupStatisticFragment())
        fragList.add(PersonalStatisticFragment())
        fragList.add(ExportFragment())
        fragList.add(ModifyPwdFragment())
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragList.get(0), "0").commit()
    }

    fun onLoginManager(view: View?) {
        initViewpager(0)
    }

    fun onReport(view: View?) {
        initViewpager(1)
    }

    fun onEntry(view: View?) {
        initViewpager(2)
    }

    fun onGroup(view: View?) {
        initViewpager(3)
    }

    fun onPersonStatistic(view: View?) {
        initViewpager(4)
    }

    fun onExport(view: View?) {
        initViewpager(5)
    }

    /**
     *  团队统计
     */
    fun showGroupStatistic(assessment: Assessment?, clazz: ClassEntity?) {
        if (fragList.size > 7) {
            fragList.removeAt(7)
        }
        val fragment =
            ReportFragment.newInstance(
                assessment = assessment,
                clazz = clazz
            )
        fragList.add(7, fragment)
        initViewpager(7)
    }

    /**
     *  个人统计
     */
    fun showPersonStatistic(studentUserId: Int, assessment: Assessment?) {
        if (fragList.size > 7) {
            fragList.removeAt(7)
        }
        val fragment =
            ReportFragment.newInstance(
                assessment = assessment,
                studentId = studentUserId
            )
        fragList.add(7, fragment)
        initViewpager(7)
    }

    fun showPersonSelect(classId: Int, assessment: Assessment?) {
        showFragment(
            PersonalSelectFragment.newInstance(
                classId = classId,
                assessment = assessment
            )
        )
    }

    private fun initViewpager(tag: Int) {
        try {
            val ft =
                supportFragmentManager!!.beginTransaction()
            ft.replace(R.id.frameLayout, fragList!![tag], tag.toString() + "")
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showFragment(fragment: Fragment) {
        try {
            val ft =
                supportFragmentManager!!.beginTransaction()
            ft.replace(R.id.frameLayout, fragment, fragment.javaClass.simpleName)
            ft.commit()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onAdminPwd(view: View?) {
        initViewpager(6)
    }

    fun onFinish(v: View) {
        finish()
    }
}