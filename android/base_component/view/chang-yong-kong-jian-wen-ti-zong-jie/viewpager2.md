# ViewPager2

## 官方Demo

Github 地址：[https://github.com/android/views-widgets-samples/tree/master/ViewPager2](https://github.com/android/views-widgets-samples/tree/master/ViewPager2)

## **与TabLayout**

\*tablayout 联动: TabLayoutMeditor

## **与Fragment使用**

- 使用FragmentStateAdapter

\* Fragment创建:不要手动创建并在外部持有fragment引用，实际上已经泄露\(原理\)

- 获取当前Fragment，可以通过fragmentManager.findFragmentByTag,tag为 "f" + holder.getItemId\(\)，具体逻辑在FragmentStateAdapter 的placeFragmentInViewHolder 方法中，需要指定itemId

\* fragment 状态恢复 ，oncreateView中恢复，onSaveInstanceState中保存数据

