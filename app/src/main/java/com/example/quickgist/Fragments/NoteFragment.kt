package com.example.quickgist.Fragments

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.example.quickgist.Activities.Login
import com.example.quickgist.Adapters.NoteAdapter
import com.example.quickgist.Models.GridModel
import com.example.quickgist.Models.NoteModel
import com.example.quickgist.R
import com.example.quickgist.uTILS.SwipeGesture
import com.example.quickgist.uTILS.hideKeyboard
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class NoteFragment : Fragment() {
    lateinit var addNoteFab: LinearLayout
    lateinit var noteUser: ImageView
    lateinit var search: EditText
    lateinit var chatFabText: TextView
    private var backPressTime: Long = 0
    lateinit var rvNote: RecyclerView
    lateinit var noData: ImageView
    lateinit var options: FirestoreRecyclerOptions<NoteModel>
    lateinit var firebaseAdapter: NoteAdapter
    lateinit var noteGrid: ImageView
    private var isGrid = false
    var layoutManager = StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
    private var backToast: Toast? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        exitTransition = MaterialElevationScale(false).apply {
            duration = 350
        }
        enterTransition = MaterialElevationScale(true).apply {
            duration = 350
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (backPressTime + 2000 > System.currentTimeMillis()) {
                    backToast?.cancel()
                    activity?.moveTaskToBack(true)
                    activity?.finish()
                    return
                } else {
                    val backToast =
                        Toast.makeText(context, "Double Press to Exit", Toast.LENGTH_SHORT)
                    backToast.show()
                }
                backPressTime = System.currentTimeMillis()

            }

        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        val view = inflater.inflate(R.layout.fragment_note, container, false)

        rvNote = view.findViewById(R.id.rev_note)
        setupFirebaseAdapter()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = Navigation.findNavController(view)
        requireView().hideKeyboard()
        CoroutineScope(Dispatchers.Main).launch {
            delay(10)
            activity?.window?.statusBarColor = resources.getColor(android.R.color.transparent)
            activity?.window?.navigationBarColor = resources.getColor(android.R.color.transparent)
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        }

            addNoteFab = view.findViewById(R.id.add_note_fab)
            rvNote = view.findViewById(R.id.rev_note)
            noData = view.findViewById(R.id.no_data)
            chatFabText = view.findViewById(R.id.chatFabText)
            noteUser = view.findViewById(R.id.note_user)
            search = view.findViewById(R.id.search)
            noteGrid = view.findViewById(R.id.note_grid)

            FirebaseFirestore.getInstance().collection("notes")
                .document(FirebaseAuth.getInstance().uid.toString())
                .addSnapshotListener { value, error ->
                    val data = value?.toObject(GridModel::class.java)
                    if (data?.isGrid != null) {
                        isGrid = data.isGrid
                    }
                    if (isGrid == true) {
                        layoutManager =
                            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        rvNote.layoutManager = layoutManager
                    } else {
                        layoutManager =
                            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                        rvNote.layoutManager = layoutManager
                    }
                    noteGrid.setOnClickListener {
                        if (isGrid == false) {
                            layoutManager =
                                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                            rvNote.layoutManager = layoutManager
                            val gridModel = GridModel()
                            isGrid = true
                            gridModel.isGrid = isGrid
                            FirebaseFirestore.getInstance().collection("notes")
                                .document(FirebaseAuth.getInstance().uid.toString()).set(gridModel)
                        } else {
                            layoutManager =
                                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
                            rvNote.layoutManager = layoutManager
                            val gridModel = GridModel()
                            isGrid = false
                            gridModel.isGrid = isGrid
                            FirebaseFirestore.getInstance().collection("notes")
                                .document(FirebaseAuth.getInstance().uid.toString()).set(gridModel)
                        }
                    }
                }



            noteUser.setOnClickListener {
                val dialog = Dialog(requireContext(),R.style.BottomSheetDialogTheme)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.account_dialog)
                dialog.show()

                val userProfile:ImageView=dialog.findViewById(R.id.user_profile)
                val userName:TextView=dialog.findViewById(R.id.user_name)
                val usermail:TextView=dialog.findViewById(R.id.user_mail)
                val userLogout:Button=dialog.findViewById(R.id.user_logout)

                Glide.with(requireContext()).load(FirebaseAuth.getInstance().currentUser?.photoUrl).into(userProfile)
                userName.text=FirebaseAuth.getInstance().currentUser?.displayName
                usermail.text=FirebaseAuth.getInstance().currentUser?.email
                userLogout.setOnClickListener {
                    val gso= GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken("1094445284031-pshs72b9uj5qqt4218jbbr2na5s2kicr.apps.googleusercontent.com").requestEmail().build()
GoogleSignIn.getClient(requireActivity(),gso).signOut()
                    startActivity(Intent(requireActivity(),Login::class.java))
                }


            }
        search.addTextChangedListener(object:TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
noData.visibility=View.GONE
                setupFirebaseAdapter()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s?.isEmpty()!!){
                    setupFirebaseAdapter()
                }
                val query =FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                    .collection("myNotes").orderBy("title").startAt(s.toString()).endAt(s.toString()+"\uF8FF")
                options=FirestoreRecyclerOptions.Builder<NoteModel>().setQuery(query,NoteModel::class.java).setLifecycleOwner(viewLifecycleOwner).build()
                firebaseAdapter= NoteAdapter(options,requireActivity())
                recyclerViewSetUp()            }

            override fun afterTextChanged(s: Editable?) {
                if(s?.isEmpty()!!){
                    setupFirebaseAdapter()
                }else{

                }
            }

        } )
        search.setOnEditorActionListener{v,actionId,_->
            if(actionId==EditorInfo.IME_ACTION_SEARCH){
                v.clearFocus()
                requireView().hideKeyboard()
            }
            return@setOnEditorActionListener true
        }
        rvNote.setOnScrollChangeListener{ _,scrllX,scrollY,_,oldscrollY->
            when {
                scrllX > oldscrollY -> {
                    chatFabText.visibility = View.GONE
                }

                scrllX == scrollY ->{
                    chatFabText.visibility=View.VISIBLE
                }
                else->{
                    chatFabText.visibility=View.VISIBLE

                }
            }
        }// to hide that add note while scrolling

        addNoteFab.setOnClickListener {
            navController.navigate(R.id.action_noteFragment_to_saveEditFragment)
        }

        swipeToGesture(rvNote)
        
        FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
            .collection("myNotes").addSnapshotListener { value, error ->
            if(value!!.isEmpty){
                noData.visibility=View.VISIBLE

            }else{
                noData.visibility=View.GONE
            }

            }

    }

    private fun swipeToGesture(rvNote: RecyclerView?) {//swipe to delete
val swipeGesture=object :SwipeGesture(requireContext()){
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        var actionbtnTapped=false
        val position=viewHolder.position
        val previousId=firebaseAdapter.snapshots.getSnapshot(position).id
        val note=firebaseAdapter.getItem(position)
        try {
            when(direction){
                ItemTouchHelper.LEFT->{
                    FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                        .collection("myNotes").document(firebaseAdapter.snapshots.getSnapshot(position).id).delete()

                    search.apply {
                        hideKeyboard()
                        clearFocus()
                    }
                    val snackbar=Snackbar.make(
                        requireView(),"Note Deleted",Snackbar.LENGTH_LONG
                    ).addCallback(object :BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                        }

                        override fun onShown(transientBottomBar: Snackbar?) {
                            transientBottomBar?.setAction("UNDO"){
                                FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                                    .collection("myNotes").document().set(note)
                                actionbtnTapped=true
                            }
                            super.onShown(transientBottomBar)
                        }
                    }).apply {
                        animationMode=Snackbar.ANIMATION_MODE_FADE
                        setAnchorView(R.id.add_note_fab)
                    }
                    snackbar.setActionTextColor(ContextCompat.getColor(requireContext(),R.color.orangeRed))
                    snackbar.show()
                }

                ItemTouchHelper.RIGHT->{
                    FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                        .collection("myNotes").document(firebaseAdapter.snapshots.getSnapshot(position).id).delete()

                    search.apply {
                        hideKeyboard()
                        clearFocus()
                    }
                    val snackbar=Snackbar.make(
                        requireView(),"Note Deleted",Snackbar.LENGTH_LONG
                    ).addCallback(object :BaseTransientBottomBar.BaseCallback<Snackbar>(){
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            super.onDismissed(transientBottomBar, event)
                        }

                        override fun onShown(transientBottomBar: Snackbar?) {
                            transientBottomBar?.setAction("UNDO"){
                                FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
                                    .collection("myNotes").document().set(note)
                                actionbtnTapped=true
                            }
                            super.onShown(transientBottomBar)
                        }
                    }).apply {
                        animationMode=Snackbar.ANIMATION_MODE_FADE
                        setAnchorView(R.id.add_note_fab)
                    }
                    snackbar.setActionTextColor(ContextCompat.getColor(requireContext(),R.color.orangeRed))
                    snackbar.show()
                }
            }

        }catch (e:Exception){
            Toast.makeText(requireContext(),e.message,Toast.LENGTH_SHORT).show()
        }
    }

}
  val touchHelper=ItemTouchHelper(swipeGesture)
  touchHelper.attachToRecyclerView(rvNote)
    }

    private fun recyclerViewSetUp() {
rvNote.setHasFixedSize(true)
        rvNote.adapter=firebaseAdapter
        rvNote.layoutManager=layoutManager
        rvNote.viewTreeObserver.addOnPreDrawListener {
            startPostponedEnterTransition()
            true
        }
    }

    private fun setupFirebaseAdapter() {
val query:Query=FirebaseFirestore.getInstance().collection("notes").document(FirebaseAuth.getInstance().uid.toString())
    .collection("myNotes").orderBy("date",Query.Direction.DESCENDING)

        options=FirestoreRecyclerOptions.Builder<NoteModel>().setQuery(query,NoteModel::class.java).setLifecycleOwner(viewLifecycleOwner).build()

        firebaseAdapter= NoteAdapter(options,requireActivity())
        recyclerViewSetUp()
    }
    private var mcontext:Context?=null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mcontext=context
    }

    override fun onDetach() {
        super.onDetach()
        mcontext=null
    }

    }

