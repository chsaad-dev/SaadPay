package com.example.saadpay.presentation.ui.main.transaction

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
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
import java.util.*

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

        viewModel.fetchTransactions()
        viewModel.transactions.observe(viewLifecycleOwner) { txns ->
            allTransactions = txns
            applyFilter(binding.filterSpinner.selectedItem.toString())
        }
    }

    private fun setupRecyclerView() {
        adapter = TransactionAdapter()
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.historyRecyclerView.adapter = adapter
    }

    private fun setupFilterSpinner() {
        val options = listOf("All", "Send", "Receive", "Load")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)
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

                // Table headers
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
        adapter.submitGroupedList(grouped)
    }

    private fun filterByDateRange() {
        if (startDate != null && endDate != null) {
            val filtered = allTransactions.filter { it.timestamp in startDate!!..endDate!! }
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
            result.addAll(txns.map { txn ->
                val labeledTxn = txn.copy(type = getTypeLabel(txn))
                TransactionListItem.Item(labeledTxn)
            })
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
