package com.example.safemvvm.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.safemvvm.R
import com.example.safemvvm.models.EmergenciesEnum
import com.example.safemvvm.models.IdBody
import com.example.safemvvm.models.TripResponse
import com.example.safemvvm.repository.Repository
import com.example.safemvvm.utils.LocalDatabaseManager
import com.example.safemvvm.utils.Navigator
import com.example.safemvvm.utils.ResponseHandler
import com.example.safemvvm.viewmodels.HomeViewModel
import com.example.safemvvm.viewmodels.HomeViewModelFactory
import com.example.safemvvm.views.voicesample.VoiceParagraphs

class HomeActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel
    private lateinit var menuButton: ImageButton
    private lateinit var menuContainer: LinearLayout
    private lateinit var profileOption: TextView
    private lateinit var trustedContactsOption: TextView
    private lateinit var reportsOption: TextView
    private lateinit var logoutOption: TextView
    private lateinit var createTripOption: TextView
    lateinit var emergencyType : EmergenciesEnum
    private lateinit var carFault : ImageView
    private lateinit var fire : ImageView
    private lateinit var harassment : ImageView
    private lateinit var kidnapping : ImageView
    private lateinit var robbery : ImageView
    private lateinit var murder :ImageView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        carFault = findViewById(R.id.iv_carFault)
        fire = findViewById(R.id.iv_fire)
        harassment = findViewById(R.id.iv_harassment)
        kidnapping = findViewById(R.id.iv_kidnapping)
        robbery = findViewById(R.id.iv_robbery)
        murder = findViewById(R.id.iv_murder)

        // Find views
        menuButton = findViewById(R.id.menu_button)
        menuContainer = findViewById(R.id.menu_container)
        profileOption = findViewById(R.id.profile_option)
        trustedContactsOption = findViewById(R.id.trusted_contacts_option)
        reportsOption = findViewById(R.id.reports_option)
        logoutOption = findViewById(R.id.logout_option)
        createTripOption = findViewById(R.id.create_trip_option)


        val localDB = getSharedPreferences("localDB", MODE_PRIVATE)
        val token = localDB.getString("token","empty")
        val userId = localDB.getInt("userId",-1)
        val savedVoice = localDB.getBoolean("saved",false)
        if(!savedVoice)
            Navigator(this).to(VoiceParagraphs::class.java).andClearStack()

        val repository = Repository()
        val viewModelFactory = HomeViewModelFactory(repository)
        viewModel = ViewModelProvider(this,viewModelFactory).get(HomeViewModel::class.java)

        viewModel.getNumOfTrusted("Bearer $token",userId)

        viewModel.checkIngoingTrip("Bearer $token",userId)

        logoutOption.setOnClickListener {

            if (token != null) {
                logoutOption.isEnabled = false
                viewModel.logout("Bearer $token", IdBody(userId))
                toggleMenu()
            }
        }

        menuButton.setOnClickListener {
            toggleMenu()
        }

        trustedContactsOption.setOnClickListener {
            Navigator(this).to(ViewTrustedContacts::class.java).andKeepStack()
            toggleMenu()
        }

        profileOption.setOnClickListener {
            Navigator(this).to(Profile::class.java).andKeepStack()
            toggleMenu()
        }

        createTripOption.setOnClickListener {
            Navigator(this).to(CreateTripActivity::class.java).andKeepStack()
            toggleMenu()
        }

        reportsOption.setOnClickListener {
            Navigator(this).to(ReportLocationMap::class.java).andKeepStack()
            toggleMenu()
        }

        carFault.setOnClickListener{
            emergencyType = EmergenciesEnum.CAR_FAULT
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        fire.setOnClickListener {
            emergencyType = EmergenciesEnum.FIRE
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        harassment.setOnClickListener{
            emergencyType = EmergenciesEnum.HARASSMENT
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        kidnapping.setOnClickListener {
            emergencyType = EmergenciesEnum.KIDNAPPING
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        robbery.setOnClickListener {
            emergencyType = EmergenciesEnum.ROBBERY
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        murder.setOnClickListener{
            emergencyType = EmergenciesEnum.MURDER
            Navigator(this).to(CheckEmergency::class.java).andPutExtraString("emergencyType", emergencyType.toString()).andClearStack()
        }

        observeResponses()
    }


    private fun toggleMenu() {
        if (menuContainer.visibility == View.VISIBLE) {
            carFault.isClickable = true
            harassment.isClickable = true
            fire.isClickable = true
            robbery.isClickable = true
            kidnapping.isClickable = true
            murder.isClickable = true

            menuContainer.visibility = View.GONE
        } else {
            carFault.isClickable = false
            harassment.isClickable = false
            fire.isClickable = false
            robbery.isClickable = false
            kidnapping.isClickable = false
            murder.isClickable = false
            menuContainer.visibility = View.VISIBLE
        }
    }

    private fun observeResponses() {
        ResponseHandler(this).observeResponse(
            viewModel.numOfContactsResponse,
            Int::class.java,
            {
                if (it == 0) {
                    Toast.makeText(this, "Please add trusted contacts.", Toast.LENGTH_LONG).show()
                    Navigator(this).to(ViewTrustedContacts::class.java).andPutExtraInt("home", 0).andClearStack()
                }
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
            }
        )

        ResponseHandler(this).observeResponse(
            viewModel.logoutResponse,
            Boolean::class.java,
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
                logoutOption.isEnabled = true
            },
            {
                LocalDatabaseManager(this).token("empty").id(-1)
                Navigator(this).to(Login::class.java).andClearStack()
                logoutOption.isEnabled = true
            }
        )

        ResponseHandler(this).observeResponse(
            viewModel.checkIngoingTripResponse,
            TripResponse::class.java,
            {
                if (it.id != -1) {
                    Navigator(this).to(WhileInTrip::class.java).andPutExtraInt("time", it.remainingTime.toInt()).andClearStack()
                }
            },
            {
                Navigator(this).to(CheckArrival::class.java).andClearStack()
            }
        )
    }
}