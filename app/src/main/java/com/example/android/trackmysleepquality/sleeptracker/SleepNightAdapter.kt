@file:Suppress("unused")

package com.example.android.trackmysleepquality.sleeptracker

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.android.trackmysleepquality.R
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.databinding.ListItemSleepNightBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

private typealias RecyclerViewHolder = RecyclerView.ViewHolder

class SleepNightAdapter(
        private val clickListener: SleepNightListener
) : ListAdapter<DataItem, RecyclerViewHolder>(
        SleepNightDiffCallback()
) {
    private class TextViewHolder(view: View): RecyclerViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): TextViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.header, parent, false)
                return TextViewHolder(view)
            }
        }
    }

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun addHeaderAndSubmitList(list: List<SleepNight>?) {
        adapterScope.launch {
            val header = listOf(DataItem.Header)
            val items = when(list) {
                null -> header
                else -> header + list.map { DataItem.SleepNightItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        ITEM_VIEW_TYPE_HEADER -> TextViewHolder.from(parent)
        ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
        else -> throw ClassCastException("Unexpected view type: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        if (holder is ViewHolder) {
            val nightItem = getItem(position) as DataItem.SleepNightItem
            holder.bind(nightItem.sleepNight, clickListener)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is DataItem.Header -> ITEM_VIEW_TYPE_HEADER
        is DataItem.SleepNightItem -> ITEM_VIEW_TYPE_ITEM
    }
}

private class SleepNightDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem) =
            oldItem.id == newItem.id

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem) =
            oldItem == newItem

}

private class ViewHolder private constructor(private val binding: ListItemSleepNightBinding)
    : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun from(parent: ViewGroup): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListItemSleepNightBinding.inflate(
                    inflater, parent, false
            )
            return ViewHolder(binding)
        }
    }

    fun bind(item: SleepNight, clickListener: SleepNightListener) {
        binding.sleep = item
        binding.clickListener = clickListener
        binding.executePendingBindings()
    }
}

sealed class DataItem {
    data class SleepNightItem(val sleepNight: SleepNight) : DataItem() {
        override val id = sleepNight.nightId
    }

    object Header : DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}