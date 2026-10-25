package com.storyteller_f.space_launcher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.storyteller_f.space_launcher.R
import com.storyteller_f.space_launcher.data.AppItem

class AppAdapter(apps: List<AppItem>, private val onItemClick: (AppItem) -> Unit) :
    ListAdapter<AppItem, AppAdapter.AppViewHolder>(AppDiff) {

    private var apps = apps.toList()
    private var query = ""

    init {
        stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY
        submitList(this.apps)
    }

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val label: TextView = view.findViewById(R.id.app_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return AppViewHolder(view).apply {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) onItemClick(getItem(position))
            }
        }
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = getItem(position)
        holder.label.text = app.label
        holder.label.setCompoundDrawables(null, app.icon, null, null)
    }

    fun filter(query: String) {
        this.query = query
        submitList(if (query.isBlank()) {
            apps
        } else {
            apps.filter { app ->
                app.label.contains(query, ignoreCase = true)
            }
        })
    }

    fun updateApps(newApps: List<AppItem>) {
        apps = newApps.toList()
        filter(query)
    }
}

internal object AppDiff : DiffUtil.ItemCallback<AppItem>() {
    override fun areItemsTheSame(oldItem: AppItem, newItem: AppItem): Boolean =
        oldItem.componentName == newItem.componentName && oldItem.user == newItem.user

    override fun areContentsTheSame(oldItem: AppItem, newItem: AppItem): Boolean =
        oldItem == newItem
}
