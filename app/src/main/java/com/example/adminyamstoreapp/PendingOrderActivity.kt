package com.example.adminyamstoreapp

import EmptyAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.adminyamstoreapp.adapter.PendingOrderAdapter
import com.example.adminyamstoreapp.databinding.ActivityPendingOrderBinding
import com.example.adminyamstoreapp.model.OrderDetails
import com.google.firebase.database.*

class PendingOrderActivity : AppCompatActivity(), PendingOrderAdapter.OnItemClicked {
    private lateinit var binding: ActivityPendingOrderBinding
    private var listOfName: MutableList<String> = mutableListOf()
    private var listOfTotalPrice: MutableList<String> = mutableListOf()
    private var listOfImageFirstFoodOrder: MutableList<String> = mutableListOf()
    private var listOfOrderItem: ArrayList<OrderDetails> = arrayListOf()
    private lateinit var database: FirebaseDatabase
    private lateinit var databaseOrderDetails: DatabaseReference
    private lateinit var adapter: PendingOrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingOrderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialization of database
        database = FirebaseDatabase.getInstance()

        // Set up empty adapter for RecyclerView
        val emptyAdapter = EmptyAdapter()
        binding.pendingOrderRecyclerView.adapter = emptyAdapter

        // Initialization of databaseReference
        databaseOrderDetails = database.reference.child("OrderDetails")

        // Set up RecyclerView layout manager
        binding.pendingOrderRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize adapter
        adapter = PendingOrderAdapter(this, listOfName, listOfTotalPrice, listOfImageFirstFoodOrder, this)
        binding.pendingOrderRecyclerView.adapter = adapter

        // Fetch and display orders
        getOrdersDetails()

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun getOrdersDetails() {
        // retrieve order details from Firebase database
        databaseOrderDetails.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (orderSnapshot in snapshot.children) {
                    val orderDetails = orderSnapshot.getValue(OrderDetails::class.java)
                    orderDetails?.let {
                        listOfOrderItem.add(it)
                    }
                }
                addDataToListForRecyclerView()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled event
            }
        })
    }

    private fun addDataToListForRecyclerView() {
        listOfName.clear()
        listOfTotalPrice.clear()
        listOfImageFirstFoodOrder.clear()
        for (orderItem in listOfOrderItem) {
            // add data to respective list for populating the recyclerView
            orderItem.userName?.let { listOfName.add(it) }
            orderItem.totalPrice?.let { listOfTotalPrice.add(it) }
            orderItem.foodImages?.filterNot { it.isEmpty() }?.forEach {
                listOfImageFirstFoodOrder.add(it)
            }
        }
        // Update adapter with new data
        adapter.updateData(listOfName, listOfTotalPrice, listOfImageFirstFoodOrder)
    }

    override fun onItemClickListener(position: Int) {
        val intent = Intent(this, OrderDetailsActivity::class.java)
        val userOrderDetails = listOfOrderItem[position]
        intent.putExtra("UserOrderDetails", userOrderDetails)
        startActivity(intent)
    }

    override fun onItemAcceptClickListener(position: Int) {
        // handle item acceptance and update database
        val childItemPushKey = listOfOrderItem[position].itemPushKey
        val clickItemOrderReference = childItemPushKey?.let {
            database.reference.child("OrderDetails").child(it)
        }
        clickItemOrderReference?.child("orderAccepted")?.setValue(true)
        updateOrderAcceptStatus(position)
    }

    override fun onItemDispatchClickListener(position: Int) {
        // handle item Dispatch and update database
        val dispatchItemPushKey = listOfOrderItem[position].itemPushKey
        val dispatchItemOrderReference =
            database.reference.child("CompletedOrder").child(dispatchItemPushKey!!)
        dispatchItemOrderReference.setValue(listOfOrderItem[position])
            .addOnSuccessListener {
                deleteThisItemFromOrderDetails(dispatchItemPushKey)
            }
    }

    private fun deleteThisItemFromOrderDetails(dispatchItemPushKey: String) {
        val orderDetailsItemsReference =
            database.reference.child("OrderDetails").child(dispatchItemPushKey)
        orderDetailsItemsReference.removeValue()
            .addOnSuccessListener {
                Toast.makeText(this, "OrderIs is Dispatched", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "OrderIs is not Dispatched", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateOrderAcceptStatus(position: Int) {
        // update order acceptance in user's BuyHistory and OrderDetails
        val userIdOfClickedItem = listOfOrderItem[position].userUid
        val pushKeyOfClickedItem = listOfOrderItem[position].itemPushKey
        val buyHistoryReference =
            database.reference.child("user").child(userIdOfClickedItem!!).child("BuyHistory")
                .child(pushKeyOfClickedItem!!)
        buyHistoryReference.child("orderAccepted").setValue(true)
        databaseOrderDetails.child(pushKeyOfClickedItem).child("orderAccepted").setValue(true)
    }
}
