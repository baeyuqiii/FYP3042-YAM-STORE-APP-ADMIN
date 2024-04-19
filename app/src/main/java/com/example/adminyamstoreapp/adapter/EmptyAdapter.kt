import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.adminyamstoreapp.R

class EmptyAdapter : RecyclerView.Adapter<EmptyAdapter.EmptyHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_empty, parent, false)
        return EmptyHolder(view)
    }

    override fun onBindViewHolder(holder: EmptyHolder, position: Int) {}

    override fun getItemCount(): Int = 0

    class EmptyHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
