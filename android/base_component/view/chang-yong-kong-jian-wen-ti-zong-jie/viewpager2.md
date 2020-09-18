# ViewPager2

## 官方Demo

Github 地址：[https://github.com/android/views-widgets-samples/tree/master/ViewPager2](https://github.com/android/views-widgets-samples/tree/master/ViewPager2)

## 基本使用

ViewPager2 是基于RecyclerView实现，因此对于普通的布局可以直接使用RecyclerView的Adapter/ViewHolder来填充。

## **与TabLayout**

实现ViewPager2 与 TabLayout联动需要用到TabLayoutMediator，注意需要在给ViewPager设置Adapter之后在设置。

```text
import com.google.android.material.tabs.TabLayoutMediator

tabLayout = findViewById(R.id.tabs)
//传入tablayout、ViewPager2实例
TabLayoutMediator(tabLayout, viewPager) { tab, position ->
    //初始化tab显示内容
    tab.text = Card.DECK[position].toString()
}.attach()
```

attach 方法实现，主要是创建ViewPager2和Tablayout的滑动监听，用于在滑动时更新两者的显示情况。

```text
public void attach() {
    if (attached) {
      throw new IllegalStateException("TabLayoutMediator is already attached");
    }
    adapter = viewPager.getAdapter();
    //需要先给ViewPager2设置Adapter
    if (adapter == null) {
      throw new IllegalStateException(
          "TabLayoutMediator attached before ViewPager2 has an " + "adapter");
    }
    attached = true;

    // 给ViewPager 设置监听
    onPageChangeCallback = new TabLayoutOnPageChangeCallback(tabLayout);
    viewPager.registerOnPageChangeCallback(onPageChangeCallback);

    // 给Tablayout 设置监听
    onTabSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
    tabLayout.addOnTabSelectedListener(onTabSelectedListener);

    // Now we'll populate ourselves from the pager adapter, adding an observer if
    // autoRefresh is enabled
    if (autoRefresh) {
      // Register our observer on the new adapter
      pagerAdapterObserver = new PagerAdapterObserver();
      adapter.registerAdapterDataObserver(pagerAdapterObserver);
    }

    //开始初始化tab
    populateTabsFromPagerAdapter();

    // 根据ViewPager2的当前位置更新TabLayout
    tabLayout.setScrollPosition(viewPager.getCurrentItem(), 0f, true);
  }
```

populateTabsFromPagerAdapter 方法会回调 onConfigureTab 方法，用于初始化所有Tab。

```text
void populateTabsFromPagerAdapter() {
    tabLayout.removeAllTabs();

    if (adapter != null) {
      int adapterCount = adapter.getItemCount();
      for (int i = 0; i < adapterCount; i++) {
        TabLayout.Tab tab = tabLayout.newTab();
        tabConfigurationStrategy.onConfigureTab(tab, i);
        tabLayout.addTab(tab, false);
      }
      // Make sure we reflect the currently set ViewPager item
      if (adapterCount > 0) {
        int lastItem = tabLayout.getTabCount() - 1;
        int currItem = Math.min(viewPager.getCurrentItem(), lastItem);
        if (currItem != tabLayout.getSelectedTabPosition()) {
          tabLayout.selectTab(tabLayout.getTabAt(currItem));
        }
      }
    }
  }
```

## **与Fragment使用**

Adapter需要使用 `androidx.viewpager2.adapter.FragmentStateAdapter` 

不要手动创建并在外部持有fragment的引用，在滑动过程中，ViewPager2会执行Fragment的销毁和重建，这时候如果外部还持有引用实际上已经发生了泄露。

应该把Fragment的创建交给Adapter：

```text
class ProfilePagerAdapter(activity: FragmentActivity, private val fragmentIds: List<Long>)
    : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = fragmentIds.size

    override fun createFragment(position: Int): Fragment {
        //创建新的Fragment
        return BaseProfileFragment.create(getItemId(position))
    }

    override fun containsItem(itemId: Long): Boolean {
        return fragmentIds.contains(itemId)
    }

    override fun getItemId(position: Int): Long {
        return fragmentIds[position]
    }
}
```

对于 fragment 销毁后的状态恢复 ，可以在 oncreateView 方法中的 savedInstanceState 恢复，在 onSaveInstanceState中保存需要保持的数据。 

获取当前Fragment，可以通过fragmentManager.findFragmentByTag,tag为 "f" + holder.getItemId\(\)，具体逻辑在FragmentStateAdapter 的placeFragmentInViewHolder 方法中，需要指定itemId

\* 



## 可变集合

