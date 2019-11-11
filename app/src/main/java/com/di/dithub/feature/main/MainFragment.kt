package com.di.dithub.feature.main

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.di.dithub.R
import com.di.dithub.base.BaseFragment
import com.di.dithub.extensions.gone
import com.di.dithub.extensions.visible
import com.di.dithub.feature.HomeModule
import com.di.dithub.feature.LauncherViewModel
import com.di.dithub.feature.main.controller.RepoController
import kotlinx.android.synthetic.main.fragment_main.*
import org.koin.android.viewmodel.ext.android.sharedViewModel
import org.koin.android.viewmodel.ext.android.viewModel

class MainFragment : BaseFragment() {
    override fun layout(): Int = R.layout.fragment_main

    companion object {
        const val TYPE_CONTENT = 1
        const val TYPE_SHIMMER = 2
    }

    private val launcherViewModel by sharedViewModel(LauncherViewModel::class)
    private val repoViewModel by viewModel(RepoViewModel::class)

    private val controller: RepoController = RepoController {
        val bundle = Bundle().apply {
            putString("URL", it.svnUrl)
        }
        findNavController().navigate(R.id.action_to_detail, bundle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyViewModel()
        setupView()

        launcherViewModel.fetchCurrUser()
    }

    private fun switchContent(type: Int) {
        when (type) {
            TYPE_CONTENT -> {
                rvContent.visible()
                shimmerLayout.gone()
                failureLayout.gone()
            }
            TYPE_SHIMMER -> {
                shimmerLayout.visible()
                rvContent.gone()
                failureLayout.gone()
                if (!shimmerLayout.isShimmerStarted) shimmerLayout.showShimmer(true)
            }
            else -> {
                failureLayout.visible()
                shimmerLayout.gone()
                rvContent.gone()
            }
        }
    }

    private fun applyViewModel() {
        launcherViewModel.apply {
            userInfo.observe(this@MainFragment, Observer {
                it?.let {
                    selectModule(HomeModule.REPOSITORIES)
                } ?: kotlin.run {
                    switchContent(-1)
                }
            })

            moduleFlag.observe(this@MainFragment, Observer {
                userInfo.value?.run {
                    switchContent(TYPE_SHIMMER)
                    repoViewModel.fetchRepos(it?.module ?: HomeModule.REPOSITORIES.module)
                }
            })
        }

        repoViewModel.apply {
            repoResult.observe(this@MainFragment, Observer {
                if (it.isNotEmpty()) {
                    if (shimmerLayout.isShimmerStarted) {
                        shimmerLayout.hideShimmer()
                    }
                    switchContent(TYPE_CONTENT)
                    controller.setData(it)
                }
            })
        }
    }

    private fun setupView() {
        rvContent.setController(controller)
    }
}