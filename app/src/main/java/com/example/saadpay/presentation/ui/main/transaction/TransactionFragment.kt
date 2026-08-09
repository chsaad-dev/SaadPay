package com.example.saadpay.presentation.ui.main.transaction

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saadpay.R
import com.example.saadpay.databinding.FragmentTransactionHistoryBinding
import com.example.saadpay.domain.model.Transaction
import com.example.saadpay.presentation.viewmodel.TransactionHistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.LinkedHashMap
import java.util.Locale

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionHistoryViewModel by viewModels()
    private lateinit var adapter: TransactionAdapter

    private var allTransactions: List<Transaction> = emptyList()
    private var startDate: Long? = null
    private var endDate: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupFilterSpinner()
        setupDatePickers()

        binding.exportPdfBtn.setOnClickListener {
            if (allTransactions.isEmpty()) {
                Toast.makeText(requireContext(), "No transactions to export", Toast.LENGTH_SHORT).show()
            } else {
                exportPdfToCacheAndShare(allTransactions)
            }
        }

        viewModel.transactions.observe(viewLifecycleOwner) { txns ->
            allTransactions = txns
            val currentFilter = binding.filterSpinner.selectedItem?.toString() ?: "All"
            applyFilter(currentFilter)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchTransactions()
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = adapter
    }

    private fun setupFilterSpinner() {
        val options = listOf("All", "Send", "Receive", "Load")
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            options
        )
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        binding.filterSpinner.adapter = spinnerAdapter

        binding.filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                applyFilter(parent.getItemAtPosition(position).toString())
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
        DatePickerDialog(requireContext(), { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            onDatePicked(cal.timeInMillis)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun formatDate(millis: Long): String {
        return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))
    }

    private fun exportPdfToCacheAndShare(transactions: List<Transaction>) {
        try {
            val fileName = "TransactionHistory_${System.currentTimeMillis()}.pdf"
            val file = File(requireContext().cacheDir, fileName)

            FileOutputStream(file).use { outputStream ->
                val writer = PdfWriter(outputStream)
                val pdfDoc = PdfDocument(writer)
                val doc = Document(pdfDoc)

                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

                doc.add(
                    Paragraph("SaadPay Transaction History")
                        .setFontSize(20f)
                        .setBold()
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(20f)
                )

                val table = Table(UnitValue.createPercentArray(floatArrayOf(3f, 2f, 2f)))
                    .useAllAvailableWidth()

                val headerColor = DeviceRgb(230, 230, 250)

                listOf("Date", "Type", "Amount").forEach {
                    table.addHeaderCell(
                        Cell().add(Paragraph(it).setBold())
                            .setBackgroundColor(headerColor)
                            .setTextAlignment(TextAlignment.CENTER)
                    )
                }

                for (txn in transactions) {
                    val type = getTypeLabel(txn)
                    table.addCell(Paragraph(sdf.format(Date(txn.timestamp))).setTextAlignment(TextAlignment.CENTER))
                    table.addCell(Paragraph(type).setTextAlignment(TextAlignment.CENTER))
                    table.addCell(Paragraph("Rs. %.2f".format(txn.amount)).setTextAlignment(TextAlignment.RIGHT))
                }

                doc.add(table)
                doc.close()
            }

            val authority = requireContext().packageName + ".provider"
            val uri = FileProvider.getUriForFile(requireContext(), authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Transaction PDF"))

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to export PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun applyFilter(filter: String) {
        val filtered = when (filter) {
            "Send" -> allTransactions.filter { getTypeLabel(it) == "Sent" }
            "Load" -> allTransactions.filter { getTypeLabel(it) == "Loaded" }
            "Receive" -> allTransactions.filter { getTypeLabel(it) == "Received" }
            else -> allTransactions
        }

        val grouped = groupTransactionsByDate(filtered)
        updateDisplayState(grouped)
    }

    private fun filterByDateRange() {
        if (startDate != null && endDate != null) {
            val filtered = allTransactions.filter { it.timestamp in startDate!!..endDate!! }
            val grouped = groupTransactionsByDate(filtered)
            updateDisplayState(grouped)
        }
    }

    private fun updateDisplayState(groupedList: List<TransactionListItem>) {
        if (groupedList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.historyRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.historyRecyclerView.visibility = View.VISIBLE
        }
        adapter.submitGroupedList(groupedList)
    }

    private fun groupTransactionsByDate(transactions: List<Transaction>): List<TransactionListItem> {
        if (transactions.isEmpty()) return emptyList()

        val sortedTransactions = transactions.sortedByDescending { it.timestamp }

        val todayCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val yesterdayCal = (todayCal.clone() as Calendar).apply { add(Calendar.DATE, -1) }
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

        fun getHeaderLabel(timestamp: Long): String {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            return when {
                cal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) -> "Today"
                cal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) -> "Yesterday"
                else -> dateFormat.format(Date(timestamp))
            }
        }

        val groupedMap = LinkedHashMap<String, MutableList<Transaction>>()
        for (txn in sortedTransactions) {
            val header = getHeaderLabel(txn.timestamp)
            groupedMap.getOrPut(header) { mutableListOf() }.add(txn)
        }

        val result = mutableListOf<TransactionListItem>()
        for ((header, txns) in groupedMap) {
            result.add(TransactionListItem.Header(header))
            for (txn in txns) {
                val labeledTxn = txn.copy(type = getTypeLabel(txn))
                result.add(TransactionListItem.Item(labeledTxn))
            }
        }
        return result
    }

    private fun getTypeLabel(txn: Transaction): String {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        return when {
            txn.receiverId == currentUserId && txn.senderId == currentUserId -> "Loaded"
            txn.receiverId == currentUserId -> "Received"
            txn.senderId == currentUserId -> "Sent"
            else -> "Transaction"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
