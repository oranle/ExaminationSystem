package com.oranle.es.module.base.examsheetdialog

import androidx.lifecycle.viewModelScope
import com.oranle.es.data.entity.Assessment
import com.oranle.es.data.entity.ClassEntity
import com.oranle.es.module.base.BaseRecycleViewModel
import com.oranle.es.module.base.IO
import com.oranle.es.module.base.UI
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class ExamSheetViewModel : BaseRecycleViewModel<Assessment>() {

    fun load() {
        viewModelScope.launch(UI) {
            val list = withContext(IO) {
                getDB().getAssessmentDao().getAllAssessments()
            }
            Timber.d("ExamSheetOperateViewModel load $list")
            notifyItem(list)
        }
    }

    fun selectAllSheet(entity: ClassEntity) {
        items.value?.forEach {
            it.isSelect.value = true
        }

        items.value?.forEach {
            Timber.d("select all sheet ${it.id}  ${it.isSelect}")
        }

        updateEntity(entity)

        notifyItem(items.value)
    }

    fun unselectAllSheet() {
        items.value?.forEach {
            it.isSelect.value = false
        }

        items.value?.forEach {
            Timber.d("select all sheet ${it.id}  ${it.isSelect}")
        }

        notifyItem(items.value)
    }

    fun selectAllShowReportSheet() {
        items.value?.forEach {
            it.showReportSheet.value = true
        }
        notifyItem(items.value)
    }

    fun unselectAllShowReportSheet() {
        items.value?.forEach {
            it.showReportSheet.value = false
        }

        notifyItem(items.value)
    }

    private fun updateEntity(entity: ClassEntity) {
        items.value?.forEach {
            Timber.d("on click ${it.id}  + ${it.isSelect} + ${it.showReportSheet}")
            if (it.isSelect.value == true) {
                entity.sheetList.add(it.id.toString())
            } else {
                entity.sheetList.remove(it.id.toString())
            }
            if (it.showReportSheet.value == true) {
                entity.showSheetReportList.add(it.id.toString())
            } else {
                entity.showSheetReportList.remove(it.id.toString())
            }
        }
    }

    fun saveToDB(entity: ClassEntity) {
        viewModelScope.launch(UI) {
            withContext(IO) {

                updateEntity(entity)

                val sheetStr = entity.sheetList.joinToString(",").replace(" ", "")
                val reportStr = entity.showSheetReportList.joinToString(",").replace(" ", "")
                Timber.d("saveToDB $entity ,   xxxxx $sheetStr")
                val copy = entity.copy(sheet = sheetStr, showSheetReport = reportStr)
                Timber.d("copy changed $copy")
                getDB().getClassDao().updateClass(copy)
            }

            toast("已修改")
        }
    }

}