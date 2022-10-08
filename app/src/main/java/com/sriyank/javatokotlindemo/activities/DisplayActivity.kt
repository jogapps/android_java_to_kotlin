package com.sriyank.javatokotlindemo.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.navigation.NavigationView
import com.sriyank.javatokotlindemo.adapters.DisplayAdapter
import com.sriyank.javatokotlindemo.retrofit.GithubAPIService
import android.os.Bundle
import com.sriyank.javatokotlindemo.R
import androidx.recyclerview.widget.LinearLayoutManager
import com.sriyank.javatokotlindemo.retrofit.RetrofitClient
import androidx.appcompat.app.ActionBarDrawerToggle
import android.util.Log
import android.view.MenuItem
import com.sriyank.javatokotlindemo.models.SearchResponse
import androidx.core.view.GravityCompat
import com.sriyank.javatokotlindemo.app.Constants
import com.sriyank.javatokotlindemo.app.Util
import com.sriyank.javatokotlindemo.models.Repository
import io.realm.Realm
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.HashMap
import kotlinx.android.synthetic.main.activity_display.*;
import kotlinx.android.synthetic.main.header.view.*

class DisplayActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var displayAdapter: DisplayAdapter
    private var browsedRepositories: List<Repository> = mutableListOf()
    private val githubAPIService: GithubAPIService by lazy {
        RetrofitClient.getGithubAPIService()
    }
    private var mRealm: Realm? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display)
        setSupportActionBar(toolbar)
        supportActionBar!!.setTitle("Showing Browsed Results")

        setAppUsername()

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        recyclerView.setLayoutManager(layoutManager)
        mRealm = Realm.getDefaultInstance()
        nav_view.setNavigationItemSelectedListener(this)
        val drawerToggle = ActionBarDrawerToggle(
            this,
            drawer_layout,
            toolbar,
            R.string.drawer_open,
            R.string.drawer_close
        )
        drawer_layout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        val intent = intent
        if (intent.getIntExtra(Constants.KEY_QUERY_TYPE, -1) == Constants.SEARCH_BY_REPO) {
            val queryRepo = intent.getStringExtra(Constants.KEY_REPO_SEARCH)
            val repoLanguage = intent.getStringExtra(Constants.KEY_LANGUAGE)
            fetchRepositories(queryRepo, repoLanguage)
        } else {
            val githubUser = intent.getStringExtra(Constants.KEY_GITHUB_USER)
            fetchUserRepositories(githubUser)
        }
    }

    private fun setAppUsername() {
        val sp = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, Context.MODE_PRIVATE)
        val personalName = sp.getString(Constants.KEY_PERSON_NAME, "User")
        val headerView = nav_view.getHeaderView(0)
        headerView.txvName.text = personalName
    }

    private fun fetchUserRepositories(githubUser: String?) {
        githubAPIService.searchRepositoriesByUser(githubUser)
            .enqueue(object : Callback<List<Repository?>?> {
                override fun onResponse(
                    call: Call<List<Repository?>?>,
                    response: Response<List<Repository?>?>
                ) {
                    if (response.isSuccessful) {
                        Log.i(TAG, "posts loaded from API $response")
                        response.body()?.let {
                            browsedRepositories = it as List<Repository>
                        }
                        //browsedRepositories = (response.body() as List<Repository>?)!!
                        if (browsedRepositories.isNotEmpty())
                            setupRecyclerView(browsedRepositories)
                        else Util.showMessage(this@DisplayActivity, "No Items Found")
                    } else {
                        Log.i(TAG, "Error $response")
                        Util.showErrorMessage(this@DisplayActivity, response.errorBody())
                    }
                }

                override fun onFailure(call: Call<List<Repository?>?>, t: Throwable) {
                    Util.showMessage(this@DisplayActivity, t.message)
                }
            })
    }

    private fun fetchRepositories(queryRepo: String?, repoLanguage: String?) {
        var queryRepo = queryRepo
        val query: MutableMap<String, String?> = HashMap()
        if (repoLanguage != null && !repoLanguage.isEmpty()) queryRepo += " language:$repoLanguage"
        query["q"] = queryRepo
        githubAPIService.searchRepositories(query).enqueue(object : Callback<SearchResponse> {
            override fun onResponse(call: Call<SearchResponse>, response: Response<SearchResponse>) {
                if (response.isSuccessful) {
                    Log.i(TAG, "posts loaded from API $response")
                    response.body()?.let {
                        browsedRepositories = it.items
                    }
                    // browsedRepositories = response.body()!!.items

                    if (browsedRepositories.isNotEmpty())
                        setupRecyclerView(browsedRepositories)
                    else Util.showMessage(this@DisplayActivity,"No Items Found")
                } else {
                    Log.i(TAG, "error $response")
                    Util.showErrorMessage(this@DisplayActivity, response.errorBody())
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                Util.showMessage(this@DisplayActivity, t.toString())
            }
        })
    }

    private fun setupRecyclerView(items: List<Repository>) {
        displayAdapter = DisplayAdapter(this, items)
        recyclerView.adapter = displayAdapter
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        menuItem.isChecked = true
        closeDrawer()
        when (menuItem.itemId) {
            R.id.item_bookmark -> {
                showBookmarks()
                supportActionBar!!.title = "Showing Bookmarks"
            }
            R.id.item_browsed_results -> {
                showBrowsedResults()
                supportActionBar!!.title = "Showing Browsed Results"
            }
        }
        return true
    }

    private fun showBrowsedResults() {
        displayAdapter.swap(browsedRepositories)
    }

    private fun showBookmarks() {
        mRealm!!.executeTransaction { realm ->
            val repositories = realm.where(
                Repository::class.java
            ).findAll()
            displayAdapter.swap(repositories)
        }
    }

    private fun closeDrawer() {
        drawer_layout.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) closeDrawer() else {
            super.onBackPressed()
            mRealm!!.close()
        }
    }

    companion object {
        private val TAG = DisplayActivity::class.java.simpleName
    }
}