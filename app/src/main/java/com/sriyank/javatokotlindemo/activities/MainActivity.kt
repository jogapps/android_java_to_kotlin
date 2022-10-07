package com.sriyank.javatokotlindemo.activities

import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import android.os.Bundle
import com.sriyank.javatokotlindemo.R
import android.content.Intent
import android.view.View
import androidx.appcompat.widget.Toolbar
import com.sriyank.javatokotlindemo.app.Constants

import kotlinx.android.synthetic.main.activity_main.*;

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
    }

    /** Save app username in SharedPreferences  */
    fun saveName(view: View?) {
        if (isNotEmpty(etName, inputLayoutName)) {
            val personName = etName.text.toString()
            val sp = getSharedPreferences(Constants.APP_SHARED_PREFERENCES, MODE_PRIVATE)
            val editor = sp.edit()
            editor.putString(Constants.KEY_PERSON_NAME, personName)
            editor.apply()
        }
    }

    /** Search repositories on github  */
    fun listRepositories(view: View?) {
        if (isNotEmpty(etRepoName, inputLayoutRepoName)) {
            val queryRepo = etRepoName!!.text.toString()
            val repoLanguage = etLanguage!!.text.toString()
            val intent = Intent(this@MainActivity, DisplayActivity::class.java)
            intent.putExtra(Constants.KEY_QUERY_TYPE, Constants.SEARCH_BY_REPO)
            intent.putExtra(Constants.KEY_REPO_SEARCH, queryRepo)
            intent.putExtra(Constants.KEY_LANGUAGE, repoLanguage)
            startActivity(intent)
        }
    }

    /** Search repositories of a particular github user  */
    fun listUserRepositories(view: View?) {
        if (isNotEmpty(etGithubUser, inputLayoutGithubUser)) {
            val githubUser = etGithubUser!!.text.toString()
            val intent = Intent(this@MainActivity, DisplayActivity::class.java)
            intent.putExtra(Constants.KEY_QUERY_TYPE, Constants.SEARCH_BY_USER)
            intent.putExtra(Constants.KEY_GITHUB_USER, githubUser)
            startActivity(intent)
        }
    }

    /** Validation  */
    private fun isNotEmpty(editText: EditText?, textInputLayout: TextInputLayout?): Boolean {
        return if (editText!!.text.toString().isEmpty()) {
            textInputLayout!!.error = "Cannot be blank !"
            false
        } else {
            textInputLayout!!.isErrorEnabled = false
            true
        }
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }
}