package org.d1scw0rld.bookbag.fileselector

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import org.d1scw0rld.bookbag.R
import java.io.File

/**
 * Create the file selection dialog. This class will create a custom dialog for
 * file selection which can be used to save files.
 */
class FileSelectorDialog : DialogFragment() {

    private var currentFileName: String? = null
    private var currentLocation: File? = null
    private var fileFilters: Array<String>? = null
    private var operation: FileOperation? = null

    // Avoid shadowing Fragment.view and clear references in onDestroyView to prevent memory leaks
    private var dialogView: View? = null
    private var fileListView: ListView? = null
    private var fileListAdapter: FileListAdapter? = null
    private var filterSpinner: Spinner? = null
    private var toolbar: Toolbar? = null
    private var fileNameEditText: EditText? = null

    /** Save or Load file listener. */
    private var onHandleFileListener: OnHandleFileListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Restore properties from arguments or savedInstanceState to survive configuration changes
        val sourceBundle = savedInstanceState ?: arguments
        sourceBundle?.let { args ->
            currentFileName = args.getString(KEY_CURRENT_FILE_NAME)
            currentLocation = args.getString(KEY_CURRENT_LOCATION)?.let { File(it) }
            fileFilters = args.getStringArray(KEY_FILE_FILTERS)
            operation = if (args.containsKey(KEY_OPERATION)) {
                @Suppress("DEPRECATION")
                args.getSerializable(KEY_OPERATION) as? FileOperation
            } else {
                null
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity(), R.style.AppCompatAlertDialogStyle)
        val context = requireContext()

        val inflatedView = View.inflate(context, R.layout.dialog_file, null)
        dialogView = inflatedView
        builder.setView(inflatedView)

        fileNameEditText = inflatedView.findViewById(R.id.fileName)
        currentFileName?.let {
            fileNameEditText?.setText(it)
        }

        toolbar = inflatedView.findViewById(R.id.dlg_toolbar)
        toolbar?.title = currentLocation?.name.orEmpty()
        toolbar?.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_new_folder -> {
                    openNewFolderDialog()
                    true
                }
                R.id.action_save, R.id.action_load -> {
                    handleSaveOrLoad()
                    true
                }
                else -> false
            }
        }

        when (operation) {
            FileOperation.SAVE -> toolbar?.inflateMenu(R.menu.menu_dialog_save)
            FileOperation.LOAD -> {
                toolbar?.inflateMenu(R.menu.menu_dialog_load)
                fileNameEditText?.isEnabled = false
            }
            null -> {}
        }

        prepareFilterSpinner(fileFilters)
        prepareFilesList()

        return builder.create()
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            val rootView = dialog.findViewById<View>(R.id.saveFileDialog)
            if (rootView != null) {
                rootView.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                var parent = rootView.parent
                while (parent is View) {
                    val layoutParams = parent.layoutParams
                    if (layoutParams != null) {
                        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                        parent.requestLayout()
                    }
                    parent = parent.parent
                }
                rootView.requestLayout()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_CURRENT_LOCATION, currentLocation?.absolutePath)
        outState.putString(KEY_CURRENT_FILE_NAME, fileNameEditText?.text?.toString() ?: currentFileName)
        outState.putSerializable(KEY_OPERATION, operation)
        outState.putStringArray(KEY_FILE_FILTERS, fileFilters)
    }

    override fun onDestroyView() {
        // Prevent memory leaks by releasing view references
        dialogView = null
        fileListView = null
        fileListAdapter = null
        filterSpinner = null
        toolbar = null
        fileNameEditText = null
        super.onDestroyView()
    }

    private fun handleSaveOrLoad() {
        val text = getSelectedFileName()
        if (checkFileName(text)) {
            val file = File(currentLocation, text)
            val messageText = when (operation) {
                FileOperation.SAVE -> {
                    if (file.exists() && !file.canWrite()) {
                        R.string.msg_cnt_sv_fl
                    } else {
                        0
                    }
                }
                FileOperation.LOAD -> {
                    when {
                        !file.exists() -> R.string.msg_msn_fl
                        !file.canRead() -> R.string.msg_acc_dnd
                        else -> 0
                    }
                }
                null -> 0
            }
            
            if (messageText != 0) {
                Toast.makeText(requireContext(), messageText, Toast.LENGTH_SHORT).apply {
                    setGravity(Gravity.CENTER, 0, 0)
                    show()
                }
            } else {
                onHandleFileListener?.handleFile(file.absolutePath)
                dismiss()
            }
        }
    }

    private fun prepareFilterSpinner(filesFilter: Array<String>?) {
        val tb = toolbar ?: return
        filterSpinner = tb.findViewById(R.id.action_select_type)
        val spinner = filterSpinner ?: return

        val filters = if (filesFilter.isNullOrEmpty()) {
            spinner.isEnabled = false
            arrayOf(FileUtils.FILTER_ALLOW_ALL)
        } else {
            filesFilter
        }

        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.layout_drop_title,
            filters
        )
        adapter.setDropDownViewResource(R.layout.layout_drop_list)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                adapterView: AdapterView<*>?,
                view: View?,
                position: Int,
                row: Long
            ) {
                if (view is TextView) {
                    val filter = view.text.toString()
                    currentLocation?.let {
                        makeList(it, filter)
                        tb.title = it.name
                    }
                }
            }

            override fun onNothingSelected(arg0: AdapterView<*>?) {}
        }
    }

    private fun prepareFilesList() {
        val inflatedView = dialogView ?: return
        fileListView = inflatedView.findViewById(R.id.fileList)
        val listView = fileListView ?: return

        listView.setOnItemClickListener { parent, _, position, _ ->
            val fileData = parent.getItemAtPosition(position) as? FileData
            if (fileData?.fileType == FileType.UP_FOLDER) {
                val parentLocation = currentLocation?.parent
                if (parentLocation != null) {
                    val selectedSpinnerView = filterSpinner?.selectedView as? TextView
                    val fileFilter = selectedSpinnerView?.text?.toString() ?: FileUtils.FILTER_ALLOW_ALL
                    val newLoc = File(parentLocation)
                    currentLocation = newLoc
                    makeList(newLoc, fileFilter)
                    toolbar?.title = newLoc.name
                } else {
                    onItemSelect(parent, position)
                }
            } else {
                onItemSelect(parent, position)
            }
        }

        val filter = filterSpinner?.selectedItem?.toString() ?: FileUtils.FILTER_ALLOW_ALL
        currentLocation?.let { makeList(it, filter) }
    }

    private fun openNewFolderDialog() {
        val context = requireContext()
        val alert = AlertDialog.Builder(context, R.style.AppCompatAlertDialogStyle)
        alert.setTitle(R.string.btn_new_fld)
        
        val input = AppCompatEditText(context)
        alert.setView(input)
        
        alert.setPositiveButton(R.string.create) { _, _ ->
            val folderName = input.text.toString().trim()
            if (folderName.isEmpty()) {
                Toast.makeText(context, R.string.msg_fld_crt_err, Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            val file = File(currentLocation, folderName)
            val messageResId = if (file.mkdirs()) R.string.msg_fld_crt_ok else R.string.msg_fld_crt_err
            Toast.makeText(context, messageResId, Toast.LENGTH_SHORT).show()

            val selectedSpinnerView = filterSpinner?.selectedView as? TextView
            val fileFilter = selectedSpinnerView?.text?.toString() ?: FileUtils.FILTER_ALLOW_ALL
            currentLocation?.let { makeList(it, fileFilter) }
        }
        alert.show()
    }

    fun getSelectedFileName(): String {
        return fileNameEditText?.text?.toString().orEmpty()
    }

    private fun onItemSelect(parent: AdapterView<*>, position: Int) {
        val fileData = parent.getItemAtPosition(position) as? FileData ?: return
        val itemText = fileData.fileName
        val itemLocation = File(currentLocation, itemText)

        if (!itemLocation.canRead()) {
            Toast.makeText(requireContext(), R.string.msg_acc_dnd, Toast.LENGTH_SHORT).show()
        } else if (itemLocation.isDirectory) {
            currentLocation = itemLocation
            val selectedSpinnerView = filterSpinner?.selectedView as? TextView
            val fileFilter = selectedSpinnerView?.text?.toString() ?: FileUtils.FILTER_ALLOW_ALL
            makeList(itemLocation, fileFilter)
            toolbar?.title = itemLocation.name
        } else if (itemLocation.isFile) {
            fileNameEditText?.setText(itemText)
        }
    }

    fun getCurrentLocation(): File? {
        return currentLocation
    }

    private fun makeList(location: File, filesFilter: String) {
        val fileList = mutableListOf<FileData>()
        val parentLocation = location.parent
        if (parentLocation != null) {
            fileList.add(FileData("../", FileType.UP_FOLDER))
        }
        
        val listFiles = location.listFiles()
        if (listFiles != null) {
            val fileDataList = mutableListOf<FileData>()
            for (tempFile in listFiles) {
                if (FileUtils.accept(tempFile, filesFilter)) {
                    val type = if (tempFile.isDirectory) FileType.FOLDER else FileType.FILE
                    fileDataList.add(FileData(tempFile.name, type))
                }
            }
            fileList.addAll(fileDataList)
            fileList.sort()
        }
        
        fileListView?.let { listView ->
            val adapter = fileListAdapter
            if (adapter == null) {
                val newAdapter = FileListAdapter(requireContext(), fileList)
                fileListAdapter = newAdapter
                listView.adapter = newAdapter
            } else {
                adapter.updateData(fileList)
            }
        }
    }

    fun checkFileName(text: String): Boolean {
        if (text.isEmpty()) {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.information)
                .setMessage(R.string.msg_fl_nm_frs)
                .setNeutralButton(android.R.string.ok, null)
                .show()
            return false
        }
        return true
    }

    companion object {
        private const val KEY_CURRENT_LOCATION = "key_current_location"
        private const val KEY_CURRENT_FILE_NAME = "key_current_file_name"
        private const val KEY_OPERATION = "key_operation"
        private const val KEY_FILE_FILTERS = "key_file_filters"

        fun newInstance(
            currentFile: File?,
            operation: FileOperation,
            onHandleFileListener: OnHandleFileListener,
            fileFilters: Array<String>?
        ): FileSelectorDialog {
            val fileSelectorDialog = FileSelectorDialog()
            fileSelectorDialog.onHandleFileListener = onHandleFileListener

            val args = Bundle().apply {
                putSerializable(KEY_OPERATION, operation)
                putStringArray(KEY_FILE_FILTERS, fileFilters)

                if (currentFile == null) {
                    @Suppress("DEPRECATION")
                    val sdCard = Environment.getExternalStorageDirectory()
                    val location = if (sdCard.canRead()) sdCard else Environment.getRootDirectory()
                    putString(KEY_CURRENT_LOCATION, location.absolutePath)
                } else {
                    if (currentFile.isDirectory) {
                        putString(KEY_CURRENT_LOCATION, currentFile.absolutePath)
                    } else {
                        putString(KEY_CURRENT_LOCATION, currentFile.parentFile?.absolutePath)
                        if (operation == FileOperation.SAVE) {
                            putString(KEY_CURRENT_FILE_NAME, currentFile.name)
                        }
                    }
                }
            }
            fileSelectorDialog.arguments = args
            return fileSelectorDialog
        }
    }
}
