package com.oranle.es.module.ui.administrator.viewmodel

import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import com.oranle.es.data.entity.*
import com.oranle.es.data.sp.SpUtil
import com.oranle.es.module.base.BaseRecycleViewModel
import com.oranle.es.module.examination.ExamDetailDialog
import com.oranle.es.module.examination.viewmodel.ExamShowMode
import com.oranle.es.module.ui.administrator.dialog.ReportDetailDialog
import com.oranle.es.module.ui.administrator.fragment.WrapReportBean
import timber.log.Timber

class GroupStatisticViewModel : BaseRecycleViewModel<WrapReportBean>() {

    val school = MutableLiveData<List<String>>()

    val classesInCharge = MutableLiveData<List<ClassEntity>>()

    val classNameList = MutableLiveData<List<String>>()

    val assessments = MutableLiveData<List<Assessment>>().apply { emptyList<User>() }

    fun loadChoiceSuit(manager: User) {
        asyncCall(
            {
                val assessmentList = getDB().getAssessmentDao().getAllAssessments()
                val temp = mutableListOf<Assessment?>()
                temp.add(null)
                temp.addAll(assessmentList)
                assessments.postValue(assessmentList)

                val schoolName = SpUtil.instance.getOrganizationName()

                schoolName?.apply {
                    school.postValue(listOf(schoolName))
                }

                val classList = mutableListOf<ClassEntity>()
                val tempClassNameList = mutableListOf<String>()
                manager.classInChargeList.forEach {
                    if (it.isNotBlank()) {
                        val classEntity = getDB().getClassDao().getClassById(it.toInt())
                        classList.add(classEntity)
                        tempClassNameList.add(classEntity.className)
                    } else {
                        Timber.w("query class by id $it")
                    }
                }

                classNameList.postValue(tempClassNameList)
                classesInCharge.postValue(classList)
            }
        )
    }

    fun loadReport(assessment: Assessment?, clazz: ClassEntity?) {
        asyncCall(
            {
                getWrapReportBean(assessment, clazz)
            },
            {
                notifyItem(it)
            }
        )
    }

    /**
     *  加载所有的报告不过滤
     */
    fun loadAllReport() {
        asyncCall(
            {
                getAllWrapReportBeanByClassIdInCharge()
            },
            {
                notifyItem(it)
            }
        )
    }

    fun showDetail(v: View, bean: WrapReportBean) {
        Timber.d("show detail ${bean}")

        val activity = v.context as FragmentActivity

        ReportDetailDialog.showDialog(activity, bean)
    }

    fun showSelfAnswer(v: View, bean: WrapReportBean) {
        Timber.d("show detail ${bean}")

        val activity = v.context as FragmentActivity

        ReportDetailDialog.showDialog(activity, bean)

        val examDetailDialog = ExamDetailDialog(
            activity,
            bean.assessment,
            ExamShowMode.AnswerShow,
            reportId = bean.reportId
        )
        examDetailDialog.show(activity.supportFragmentManager, "")
    }

    fun delete(bean: WrapReportBean) {
        Timber.d("delete ${bean}")

        asyncCall(
            {
                getDB().getReportDao().deleteReportById(bean.reportId)
            }, {
                toast("已删除")
                loadAllReport()
            }
        )
    }

    fun onNextStep() {

    }

    private suspend fun getAllWrapReportBeanByClassIdInCharge(): List<WrapReportBean> {

        val currentUser = SpUtil.instance.getCurrentUser()

        if (currentUser == null) {
            toast("当前登录用户为空，请检查")
            return emptyList()
        }

        val classesInCharge = getAllClassesInCharge(currentUser)

        val classesIdInCharge = mutableListOf<Int>()
        classesInCharge.forEach {
            classesIdInCharge.add(it.id)
        }

        val allStudents = getAllStudentByClassIds(classesIdInCharge)

        val stuIds = mutableListOf<Int>()
        allStudents.forEach {
            stuIds.add(it.id)
        }

        return generateWrapReportBean(stuIds, allStudents, classesInCharge)
    }

    private suspend fun generateWrapReportBean(
        stuIds: MutableList<Int>,
        allStudents: List<User>,
        classesInCharge: List<ClassEntity>
    ): MutableList<WrapReportBean> {
        val reports = getDB().getReportDao().getReportsByUserIds(stuIds)

        val wrapReportBeans = mutableListOf<WrapReportBean>()

        val allAssessments = getDB().getAssessmentDao().getAllAssessments()

        val allRuleList = mutableListOf<List<ReportRule>>()
        allAssessments.forEach {
            val rules = getDB().getRuleDao().getRulesBySheetId(it.id)
            allRuleList.add(rules)
        }

        reports.forEachIndexed { index, it ->
            val student = getStudentById(allStudents, it.userId)
            val classEntity = getClassById(classesInCharge, student.classId)
            val scoreList = it.getTypedScore
            val assessment = getAssessmentById(allAssessments, it.sheetId)
            val rules = getRulesById(allRuleList, it.sheetId)
            wrapReportBeans.add(
                WrapReportBean(
                    reportId = it.id,
                    index = index + 1,
                    user = student,
                    clazz = classEntity,
                    time = it.testTime,
                    assessment = assessment,
                    typedScore = scoreList,
                    rules = rules
                )
            )
        }
        return wrapReportBeans
    }

    private suspend fun getWrapReportBean(
        assessment: Assessment?,
        clazz: ClassEntity?
    ): List<WrapReportBean> {

        val currentUser = SpUtil.instance.getCurrentUser()

        if (currentUser == null) {
            toast("当前登录用户为空，请检查")
            return emptyList()
        }

        val (classesInCharge, allStudents, stuIds) = getTripleData(clazz, currentUser)

        // get report from db
        val reports =
            if (assessment != null)
                getDB().getReportDao().getReportsByUserIdAndSheetId(assessment.id, stuIds)
            else
                getDB().getReportDao().getReportsByUserIds(stuIds)

        // get rule from db
        val ruleList = mutableListOf<List<ReportRule>>()

        val allAssessments = getDB().getAssessmentDao().getAllAssessments()
        if (assessment == null) {
            allAssessments.forEach {
                val rules = getDB().getRuleDao().getRulesBySheetId(it.id)
                ruleList.add(rules)
            }
        } else {
            val list = getDB().getRuleDao().getRulesBySheetId(assessment.id)
            ruleList.add(list)
        }

        val wrapReportBeans = mutableListOf<WrapReportBean>()
        reports.forEachIndexed { index, it ->
            val student = getStudentById(allStudents, it.userId)
            val classEntity = getClassById(classesInCharge, student.classId)
            val scoreList = it.getTypedScore
            val tempAssessment = getAssessmentById(allAssessments, it.sheetId)
            val rule = getRulesById(ruleList, tempAssessment.id)
            wrapReportBeans.add(
                WrapReportBean(
                    reportId = it.id,
                    index = index + 1,
                    user = student,
                    clazz = classEntity,
                    time = it.testTime,
                    assessment = tempAssessment,
                    typedScore = scoreList,
                    rules = rule
                )
            )
        }

        return wrapReportBeans
    }

    private suspend fun getTripleData(
        clazz: ClassEntity?,
        currentUser: User
    ): Triple<MutableList<ClassEntity>, List<User>, MutableList<Int>> {

        val allStudents = mutableListOf<User>()
        val stuIds = mutableListOf<Int>()
        val classesInCharge = mutableListOf<ClassEntity>()

        if (currentUser.role == Role.Manager.value) {
            // 未指定班级信息则表示选择全部
            if (clazz == null) {
                classesInCharge.addAll(getAllClassesInCharge(currentUser))
            } else {
                classesInCharge.add(clazz)
            }

            val classesIdInCharge = mutableListOf<Int>()
            classesInCharge.forEach {
                classesIdInCharge.add(it.id)
            }

            val examinees = getAllStudentByClassIds(classesIdInCharge)
            allStudents.addAll(examinees)

            allStudents.forEach {
                stuIds.add(it.id)
            }
        } else if (currentUser.role == Role.Examinee.value) {
            classesInCharge.add(getDB().getClassDao().getClassById(currentUser.classId))
            allStudents.add(currentUser)
            stuIds.add(currentUser.id)
        }
        return Triple(classesInCharge, allStudents, stuIds)
    }

    private fun getStudentById(students: List<User>, specifyId: Int): User {
        students.forEach {
            if (it.id == specifyId) {
                return it
            }
        }
        throw IllegalArgumentException("can not find user with id: $specifyId")
    }

    private fun getClassById(classes: List<ClassEntity>, specifyId: Int): ClassEntity {
        classes.forEach {
            if (it.id == specifyId) {
                return it
            }
        }
        throw IllegalArgumentException("can not find ClassEntity with id: $specifyId")
    }

    private fun getAssessmentById(assessments: List<Assessment>, specifyId: Int): Assessment {
        assessments.forEach {
            if (it.id == specifyId) {
                return it
            }
        }
        throw IllegalArgumentException("can not find Assessment with id: $specifyId")
    }

    private fun getRulesById(allRules: List<List<ReportRule>>, specifyId: Int): List<ReportRule> {
        allRules.forEach { typeRules ->
            typeRules.forEach {
                if (it.sheetId == specifyId) {
                    return typeRules
                }
            }
        }
        throw IllegalArgumentException("can not find Rules with id: $specifyId")
    }

    /**
     * 根据班级id查出所有的学生
     */
    private suspend fun getAllStudentByClassIds(classIds: List<Int>): List<User> =
        getDB().getUserDao().getUserByClassIdsAndRole(classIds, Role.Examinee.value)

    /**
     *  获取指定集合中的id的，且指定表id的报告
     */

    private suspend fun getAllClassesInCharge(manager: User): List<ClassEntity> {
        val classList = mutableListOf<ClassEntity>()
        manager.classInChargeList.forEach {
            if (it.isNotBlank()) {
                val classEntity = getDB().getClassDao().getClassById(it.toInt())
                classList.add(classEntity)
            } else {
                Timber.w("query class by id $it")
            }
        }
        return classList
    }

}