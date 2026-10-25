package com.storyteller_f.space_launcher.ui

import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.os.UserHandle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.storyteller_f.space_launcher.R

internal data class WidgetPickerItem(
    val info: AppWidgetProviderInfo,
    val label: String,
    val appLabel: String,
    val minWidth: Int,
    val minHeight: Int,
    val iconResource: Int,
    val provider: ComponentName = info.provider,
    val profile: UserHandle = info.profile
)

internal object WidgetPickerDiff : DiffUtil.ItemCallback<WidgetPickerItem>() {
    override fun areItemsTheSame(oldItem: WidgetPickerItem, newItem: WidgetPickerItem): Boolean =
        oldItem.provider == newItem.provider && oldItem.profile == newItem.profile

    override fun areContentsTheSame(oldItem: WidgetPickerItem, newItem: WidgetPickerItem): Boolean =
        oldItem.label == newItem.label && oldItem.appLabel == newItem.appLabel &&
            oldItem.minWidth == newItem.minWidth && oldItem.minHeight == newItem.minHeight &&
            oldItem.iconResource == newItem.iconResource
}

internal class WidgetPickerAdapter(
    private val onClick: (AppWidgetProviderInfo) -> Unit
) : ListAdapter<WidgetPickerItem, WidgetPickerAdapter.ViewHolder>(WidgetPickerDiff) {
    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.widget_icon)
        val label: TextView = view.findViewById(R.id.widget_label)
        val appName: TextView = view.findViewById(R.id.widget_app_name)
        val size: TextView = view.findViewById(R.id.widget_size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_widget_picker, parent, false)).apply {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onClick(getItem(position).info)
            }
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val context = holder.itemView.context
        try {
            holder.icon.setImageDrawable(item.info.loadIcon(context, context.resources.displayMetrics.densityDpi))
        } catch (e: Exception) {
            holder.icon.setImageResource(android.R.drawable.sym_def_app_icon)
        }
        holder.label.text = item.label
        holder.appName.text = item.appLabel
        holder.size.text = context.getString(R.string.widget_minimum_size, item.minWidth, item.minHeight)
    }
}
