package com.example.c001apk.ui.fragment.home.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.ThemeUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cc.shinichi.library.ImagePreview
import cc.shinichi.library.bean.ImageInfo
import com.example.c001apk.R
import com.example.c001apk.databinding.FragmentHomeFeedBinding
import com.example.c001apk.ui.fragment.home.HomeFragment
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnBottomClickListener
import com.example.c001apk.ui.fragment.minterface.IOnFeedPicClickContainer
import com.example.c001apk.ui.fragment.minterface.IOnFeedPicClickListener
import com.example.c001apk.util.LinearItemDecoration
import com.example.c001apk.util.TokenDeviceUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class HomeFeedFragment : Fragment(), IOnBottomClickListener, IOnFeedPicClickListener {

    private lateinit var binding: FragmentHomeFeedBinding
    private val viewModel by lazy { ViewModelProvider(this)[HomeFeedViewModel::class.java] }
    private lateinit var mAdapter: HomeFeedAdapter
    private lateinit var mLayoutManager: LinearLayoutManager
    private var firstCompletelyVisibleItemPosition = 0
    private var lastVisibleItemPosition = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initData()
        initRefresh()
        initScroll()

        viewModel.homeFeedData.observe(viewLifecycleOwner) { result ->
            val feed = result.getOrNull()
            if (!feed.isNullOrEmpty()) {
                if (viewModel.isRefreshing)
                    viewModel.homeFeedList.clear()
                if (viewModel.isRefreshing || viewModel.isLoadMore) {
                    for (element in feed) {
                        if (element.entityTemplate == "feed"
                            || element.entityTemplate == "iconMiniScrollCard"
                            || element.entityTemplate == "iconLinkGridCard"
                            || element.entityTemplate == "imageCarouselCard_1"
                            || element.entityTemplate == "imageTextScrollCard")
                        /*if (element.entityTemplate != "sponsorCard"
                            && element.entityTemplate != "refreshCard"
                            && element.entityTemplate != "listCard"
                            )*/
                            viewModel.homeFeedList.add(element)
                    }
                    viewModel.lastItem = feed[feed.size - 1].entityId
                }
                mAdapter.notifyDataSetChanged()
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
            } else {
                viewModel.isLoadMore = false
                viewModel.isRefreshing = false
                binding.swipeRefresh.isRefreshing = false
                Toast.makeText(activity, "没有更多了", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        }

    }

    private fun initScroll() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (lastVisibleItemPosition == viewModel.homeFeedList.size - 1) {
                        viewModel.isLoadMore = true
                        viewModel.page++
                        viewModel.firstLaunch = 0
                        viewModel.getHomeFeed()
                    }
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                lastVisibleItemPosition = mLayoutManager.findLastVisibleItemPosition()
                firstCompletelyVisibleItemPosition =
                    mLayoutManager.findFirstCompletelyVisibleItemPosition()
            }
        })
    }

    @SuppressLint("RestrictedApi")
    private fun initRefresh() {
        binding.swipeRefresh.setColorSchemeColors(
            ThemeUtils.getThemeAttrColor(
                requireActivity(),
                rikka.preference.simplemenu.R.attr.colorPrimary
            )
        )
        binding.swipeRefresh.setOnRefreshListener {
            refreshData()
        }
    }

    private fun initData() {
        if (viewModel.homeFeedList.isEmpty())
            refreshData()
    }

    private fun refreshData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.page = 1
        viewModel.firstLaunch = 1
        viewModel.isRefreshing = true
        viewModel.isLoadMore = false
        viewModel.installTime = TokenDeviceUtils.getLastingInstallTime(requireActivity())
        lifecycleScope.launch {
            delay(500)
            viewModel.getHomeFeed()
        }
    }

    private fun initView() {
        val space = resources.getDimensionPixelSize(R.dimen.normal_space)
        mAdapter = HomeFeedAdapter(requireActivity(), viewModel.homeFeedList)
        mLayoutManager = LinearLayoutManager(activity)
        binding.recyclerView.apply {
            adapter = mAdapter
            layoutManager = mLayoutManager
            if (itemDecorationCount == 0)
                addItemDecoration(LinearItemDecoration(space))
        }
    }

    override fun onReturnTop() {
        if (HomeFragment.current == 2){
            if (firstCompletelyVisibleItemPosition == 0)
                refreshData()
            else {
                binding.recyclerView.scrollToPosition(0)
                refreshData()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as IOnBottomClickContainer).controller = this
        IOnFeedPicClickContainer.controller = this
    }

    override fun onShowPic(position: Int, urlList: MutableList<ImageInfo>) {
        ImagePreview.instance
            .setContext(requireActivity())
            .setImageInfoList(urlList)
            .setIndex(position)
            .setShowCloseButton(true)
            .setEnableDragClose(true)
            .setEnableUpDragClose(true)
            .setFolderName("c001apk")
            .start()
    }

}