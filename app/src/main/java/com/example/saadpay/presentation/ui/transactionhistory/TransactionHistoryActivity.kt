package com.example.saadpay.presentation.ui.transactionhistory

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saadpay.data.model.Transaction
import com.example.saadpay.databinding.ActivityTransactionHistoryBinding
import com.example.saadpay.presentation.viewmodel.TransactionHistoryViewModel
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.text.SimpleDateFormat
import java.util.*

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTransactionHistoryBinding
    private val viewModel: TransactionHistoryViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter
    private var allTransactions: List<Transaction> = emptyList()

    private var startDate: Long? = null
    private var endDate: Long? = null

    // ActivityResultLauncher for Create Document Intent
    private lateinit var createPdfLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFilterSpinner()
        setupDatePickers()

        // Initialize the ActivityResultLauncher
        createPdfLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data
                if (uri != null) {
                    savePdfToUri(uri, allTransactions)
                } else {
                    Toast.makeText(this, "Save cancelled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Save cancelled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.exportPdfBtn.setOnClickListener {
            if (allTransactions.isEmpty()) {
                Toast.makeText(this, "No transactions to export", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Launch save dialog
            launchSavePdfDialog()
        }

        viewModel.fetchTransactions()

        viewModel.transactions.observe(this) { txns ->
            allTransactions = txns
            applyFilter(binding.filterSpinner.selectedItem.toString())
            if (txns.isEmpty()) {
                Toast.makeText(this, "No transactions found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun launchSavePdfDialog() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/pdf"
            putExtra(Intent.EXTRA_TITLE, "TransactionHistory_${System.currentTimeMillis()}.pdf")
        }
        createPdfLauncher.launch(intent)
    }

    private fun savePdfToUri(uri: Uri, transactions: List<Transaction>) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdfDoc = PdfDocument(writer)
                val doc = Document(pdfDoc)

                doc.add(
                    Paragraph("Transaction History")
                        .setBold()
                        .setFontSize(20f)
                        .setMarginBottom(20f)
                )

                transactions.forEach {
                    val line = "${it.type} - Rs.${it.amount} on ${
                        SimpleDateFormat(
                            "dd MMM yyyy, hh:mm a",
                            Locale.getDefault()
                        ).format(Date(it.timestamp))
                    }"
                    doc.add(Paragraph(line))
                }

                doc.close()
                Toast.makeText(this, "PDF saved successfully", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = adapter
    }

    private fun setupFilterSpinner() {
        val filterOptions = listOf("All", "Send", "Load", "Receive")
        val spinnerAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterOptions)
        binding.filterSpinner.adapter = spinnerAdapter

        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selected = parent.getItemAtPosition(position).toString()
                applyFilter(selected)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePickers() {
        binding.startDateBtn.setOnClickListener {
            showDatePicker { millis ->
                startDate = millis
                binding.startDateBtn.text = formatDate(millis)
                filterByDateRange()
            }
        }

        binding.endDateBtn.setOnClickListener {
            showDatePicker { millis ->
                endDate = millis
                binding.endDateBtn.text = formatDate(millis)
                filterByDateRange()
            }
        }
    }

    private fun showDatePicker(onDatePicked: (Long) -> Unit) {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this, { _, y, m, d ->
                cal.set(y, m, d, 0, 0, 0)
                onDatePicked(cal.timeInMillis)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "Send" -> allTransactions.filter { it.type == "Send" }
            "Load" -> allTransactions.filter { it.type == "Load" }
            "Receive" -> allTransactions.filter { it.type == "Receive" }
            else -> allTransactions
        }

        val grouped = groupTransactionsByDate(filtered)
        adapter.submitGroupedList(grouped)
    }

    private fun filterByDateRange() {
        if (startDate != null && endDate != null) {
            val filtered = allTransactions.filter {
                it.timestamp in startDate!!..endDate!!
            }
            val grouped = groupTransactionsByDate(filtered)
            adapter.submitGroupedList(grouped)
        }
    }

    private fun groupTransactionsByDate(transactions: List<Transaction>): List<TransactionListItem> {
        val groupedMap = transactions.groupBy {
            val date = Date(it.timestamp)
            val cal = Calendar.getInstance().apply { time = date }

            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DATE, -1) }

            when {
                cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR) -> "Today"

                cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR) -> "Yesterday"

                else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
            }
        }

        val result = mutableListOf<TransactionListItem>()
        groupedMap.toSortedMap(compareByDescending { parseDate(it) }).forEach { (date, txns) ->
            result.add(TransactionListItem.Header(date))
            result.addAll(txns.map { TransactionListItem.Item(it) })
        }
        return result
    }

    private fun parseDate(label: String): Date {
        return when (label) {
            "Today" -> Calendar.getInstance().time
            "Yesterday" -> Calendar.getInstance().apply { add(Calendar.DATE, -1) }.time
            else -> SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(label) ?: Date(0)
        }
    }
}
