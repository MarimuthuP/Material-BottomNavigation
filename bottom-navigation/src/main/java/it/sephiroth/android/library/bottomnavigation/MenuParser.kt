package it.sephiroth.android.library.bottomnavigation

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Xml
import it.sephiroth.android.library.bottonnavigation.R
import org.xmlpull.v1.XmlPullParser
import java.util.*

/**
 * Created by alessandro crugnola on 4/3/16 at 7:59 PM.
 * Project: MaterialBottomNavigation
 *
 * The MIT License
 */
class MenuParser {

    private var item: MenuItem? = null
    private var menu: Menu? = null

    @Suppress("unused")
    class Menu(private val context: Context) {

        var items: Array<BottomNavigationItem>? = null

        var colorActive: Int = 0
        var background: Int = 0
        var rippleColor: Int = 0
        var colorInactive: Int = 0
        var colorDisabled: Int = 0

        var itemAnimationDuration: Int = 0
            internal set

        var badgeColor: Int = 0
            internal set

        val itemsCount: Int
            get() = this.items?.size ?: 0

        override fun toString(): String {
            return "Menu{background:$background, colorActive:$colorActive, colorInactive:$colorInactive, colorDisabled: $colorDisabled}"
        }

        fun getItemAt(index: Int): BottomNavigationItem {
            return this.items!![index]
        }

        /**
         * Returns true if the first item of the menu
         * has a color defined
         */
        fun hasChangingColor(): Boolean {
            return this.items!![0].hasColor()
        }
    }

    data class MenuItem(
            var itemId: Int = 0,
            var itemTitle: CharSequence? = null,
            var itemIconResId: Int = 0,
            var isItemEnabled: Boolean = false,
            var itemColor: Int = 0
                       )

    private fun readMenu(context: Context, attrs: AttributeSet) {
        menu = Menu(context)
        val a = context.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenu)

        menu?.let {
            it.itemAnimationDuration =
                    a.getInt(R.styleable.BottomNavigationMenu_bbn_itemAnimationDuration, context.resources.getInteger(R.integer.bbn_item_animation_duration))

            if (a.hasValue(R.styleable.BottomNavigationMenu_android_background)) {
                it.background = a.getColor(R.styleable.BottomNavigationMenu_android_background, 0)
            } else {
                it.background = MiscUtils.getColor(context, android.R.attr.windowBackground)
            }

            if (a.hasValue(R.styleable.BottomNavigationMenu_bbn_rippleColor)) {
                it.rippleColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_rippleColor, 0)
            } else {
                it.rippleColor = MiscUtils.getColor(context, R.attr.colorControlHighlight)
            }

            if (a.hasValue(R.styleable.BottomNavigationMenu_bbn_itemColorActive)) {
                it.colorActive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorActive, 0)
            } else {
                it.colorActive = MiscUtils.getColor(context, android.R.attr.colorForeground)
            }

            if (a.hasValue(R.styleable.BottomNavigationMenu_bbn_itemColorInactive)) {
                it.colorInactive = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorInactive, 0)
            } else {
                val color = it.colorActive
                it.colorInactive = Color.argb(Color.alpha(color) / 2, Color.red(color), Color.green(color), Color.blue(color))
            }

            if (a.hasValue(R.styleable.BottomNavigationMenu_bbn_itemColorDisabled)) {
                it.colorDisabled = a.getColor(R.styleable.BottomNavigationMenu_bbn_itemColorDisabled, 0)
            } else {
                val color = it.colorInactive
                it.colorDisabled = Color.argb(Color.alpha(color) / 2, Color.red(color), Color.green(color), Color.blue(color))

            }

            it.badgeColor = a.getColor(R.styleable.BottomNavigationMenu_bbn_badgeColor, Color.RED)
        }
        a.recycle()
    }

    fun pullItem(): MenuItem? {
        val current = item
        item = null
        return current
    }

    fun hasItem(): Boolean {
        return null != item
    }

    fun hasMenu(): Boolean {
        return null != menu
    }

    private fun pullMenu(): Menu? {
        val current = menu
        menu = null
        return current
    }

    /**
     * Called when the parser is pointing to an item tag.
     */
    fun readItem(mContext: Context, attrs: AttributeSet) {
        val a = mContext.obtainStyledAttributes(attrs, R.styleable.BottomNavigationMenuItem)
        item = MenuItem()
        item?.let {
            it.itemId = a.getResourceId(R.styleable.BottomNavigationMenuItem_android_id, 0)
            it.itemTitle = a.getText(R.styleable.BottomNavigationMenuItem_android_title)
            it.itemIconResId = a.getResourceId(R.styleable.BottomNavigationMenuItem_android_icon, 0)
            it.isItemEnabled = a.getBoolean(R.styleable.BottomNavigationMenuItem_android_enabled, true)
            it.itemColor = a.getColor(R.styleable.BottomNavigationMenuItem_android_color, 0)
        }
        a.recycle()
    }

    companion object {
        fun inflateMenu(context: Context, menuRes: Int): Menu? {
            val list = ArrayList<BottomNavigationItem>()

            val menuParser = MenuParser()

            try {
                val parser = context.resources.getLayout(menuRes)
                val attrs = Xml.asAttributeSet(parser)

                var tagName: String
                var eventType = parser.eventType
                var lookingForEndOfUnknownTag = false
                var unknownTagName: String? = null

                do {
                    if (eventType == XmlPullParser.START_TAG) {
                        tagName = parser.name
                        if (tagName == "menu") {
                            menuParser.readMenu(context, attrs)
                            eventType = parser.next()
                            break
                        }
                        throw RuntimeException("Expecting menu, got $tagName")
                    }
                    eventType = parser.next()
                } while (eventType != XmlPullParser.END_DOCUMENT)

                var reachedEndOfMenu = false

                loop@ while (!reachedEndOfMenu) {
                    when (eventType) {
                        XmlPullParser.START_TAG -> {
                            if (lookingForEndOfUnknownTag) {
                                break@loop
                            }
                            tagName = parser.name
                            if (tagName == "item") {
                                menuParser.readItem(context, attrs)
                            } else {
                                lookingForEndOfUnknownTag = true
                                unknownTagName = tagName
                            }
                        }

                        XmlPullParser.END_TAG -> {
                            tagName = parser.name
                            if (lookingForEndOfUnknownTag && tagName == unknownTagName) {
                                lookingForEndOfUnknownTag = false
                                unknownTagName = null
                            } else if (tagName == "item") {
                                if (menuParser.hasItem()) {
                                    val item = menuParser.pullItem()
                                    val tab = BottomNavigationItem(item!!.itemId, item.itemIconResId,
                                            item.itemTitle.toString()
                                                                  )
                                    tab.isEnabled = item.isItemEnabled
                                    tab.color = item.itemColor
                                    list.add(tab)
                                }
                            } else if (tagName == "menu") {
                                reachedEndOfMenu = true
                            }
                        }

                        XmlPullParser.END_DOCUMENT -> throw RuntimeException("Unexpected end of document")

                        else -> {
                        }
                    }
                    eventType = parser.next()
                }
            } catch (e: Exception) {
                return null
            }

            if (menuParser.hasMenu()) {
                val menu = menuParser.pullMenu()
                menu!!.items = list.toTypedArray()
                return menu
            }

            return null
        }
    }
}
