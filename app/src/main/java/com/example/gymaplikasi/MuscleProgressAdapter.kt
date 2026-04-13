package com.example.gymaplikasi

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gymaplikasi.domain.ProgressListItem

class MuscleProgressAdapter(
    private val getScoreColor: (Int) -> Int,
    private val getRankIcon: (Int) -> Int,
    private val getRankName: (Int) -> String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val displayList = mutableListOf<ProgressListItem>()

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CHILD = 1
    }

    fun submitList(headers: List<ProgressListItem.MuscleHeader>) {
        displayList.clear()
        displayList.addAll(headers)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (displayList[position]) {
            is ProgressListItem.MuscleHeader -> TYPE_HEADER
            is ProgressListItem.ExerciseChild -> TYPE_CHILD
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_HEADER) {
            val view = inflater.inflate(R.layout.item_muscle_header, parent, false)
            HeaderViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_exercise_child, parent, false)
            ChildViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = displayList[position]
        if (holder is HeaderViewHolder && item is ProgressListItem.MuscleHeader) {
            holder.bind(item)
        } else if (holder is ChildViewHolder && item is ProgressListItem.ExerciseChild) {
            holder.bind(item)
        }
    }

    override fun getItemCount(): Int = displayList.size

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvMuscleName)
        private val ivIcon: ImageView = itemView.findViewById(R.id.ivMuscleIcon)
        private val pbProgress: ProgressBar = itemView.findViewById(R.id.pbMuscleProgress)
        private val ivRank: ImageView = itemView.findViewById(R.id.ivRankIconHeader)
        private val tvRankName: TextView = itemView.findViewById(R.id.tvRankNameHeader)

        fun bind(header: ProgressListItem.MuscleHeader) {
            tvName.text = header.muscle.name
            ivIcon.setImageResource(header.iconResId)

            pbProgress.progress = header.overallScore
            val color = getScoreColor(header.overallScore)
            pbProgress.progressTintList = ColorStateList.valueOf(color)

            ivRank.setImageResource(getRankIcon(header.overallScore))
            tvRankName.text = getRankName(header.overallScore)

            if (header.overallScore == 0) {
                tvRankName.setTextColor(android.graphics.Color.parseColor("#888888"))
            } else {
                tvRankName.setTextColor(color)
            }

            itemView.setOnClickListener {
                val currentPosition = bindingAdapterPosition
                if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                if (header.isExpanded) {
                    header.isExpanded = false
                    displayList.removeAll(header.exercises)
                    notifyItemRangeRemoved(currentPosition + 1, header.exercises.size)
                } else {
                    header.isExpanded = true
                    displayList.addAll(currentPosition + 1, header.exercises)
                    notifyItemRangeInserted(currentPosition + 1, header.exercises.size)
                }
            }
        }
    }

    inner class ChildViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvExerciseName)
        private val tvWeight: TextView = itemView.findViewById(R.id.tvExerciseWeight)
        private val pbProgress: ProgressBar = itemView.findViewById(R.id.pbExerciseProgress)
        private val ivRank: ImageView = itemView.findViewById(R.id.ivExerciseRankIcon)
        private val tvNextTarget: TextView = itemView.findViewById(R.id.tvNextTarget)

        fun bind(child: ProgressListItem.ExerciseChild) {
            tvName.text = child.exerciseName
            tvWeight.text = "${child.weightKg} Kg"

            pbProgress.progress = child.score
            pbProgress.progressTintList = ColorStateList.valueOf(getScoreColor(child.score))
            ivRank.setImageResource(getRankIcon(child.score))

            if (child.score >= 90) {
                // Jika sudah Mythril (Rank Tertinggi)
                tvNextTarget.text = "Max Rank Reached! 🔥"
                tvNextTarget.setTextColor(android.graphics.Color.parseColor("#B1EE2E")) // Hijau Stabilo
            } else {
                // Jika masih ada target berikutnya
                tvNextTarget.text = "Target: ${child.nextRankKg} Kg ke ${child.nextRankName}"
                tvNextTarget.setTextColor(android.graphics.Color.parseColor("#888888")) // Abu-abu italic
            }
        }
    }
}