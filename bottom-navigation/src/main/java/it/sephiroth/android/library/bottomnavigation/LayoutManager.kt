package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import it.sephiroth.android.library.bottonnavigation.R

/**
 * Created by alessandro on 4/4/16 at 11:13 PM.
 * Project: Material-BottomNavigation
 */
abstract class LayoutManager @JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0) :
        ViewGroup(context, attrs, defStyleAttr) {

    protected var distributeEqually: Boolean

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigation, defStyleAttr, defStyleRes)
        distributeEqually = array.getBoolean(R.styleable.BottomNavigation_bbn_distributeEqually, true)
        array.recycle()
    }

    abstract fun getSelectedIndex(): Int

    abstract fun setSelectedIndex(index: Int, animate: Boolean)

    abstract fun populate(menu: MenuParser.Menu)

    var itemClickListener: OnItemClickListener? = null

    abstract fun removeAll()

    abstract fun setItemEnabled(index: Int, enabled: Boolean)
}
