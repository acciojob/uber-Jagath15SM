package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.delete(customerRepository2.findById(customerId).get());
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> drivers = driverRepository2.findAll();
		Customer customer = customerRepository2.findById(customerId).get();
		Collections.sort(drivers, (d1, d2) -> d1.getDriverId() - d2.getDriverId());
		TripBooking tripBooking = new TripBooking(fromLocation, toLocation, distanceInKm);
		for(Driver d : drivers){
			if(d.getCab().getAvailable()){
				d.getCab().setAvailable(false);
				tripBooking.setDriver(d);
				tripBooking.setStatus(TripStatus.CONFIRMED);
				driverRepository2.save(d);
				break;
			}
		}
		customer.getTripBookingList().add(tripBooking);
		tripBooking.setCustomer(customer);
		if(tripBooking.getDriver() == null){
			throw new Exception("No cab available!");
		}
		tripBookingRepository2.save(tripBooking);
		customerRepository2.save(customer);
		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking trip = tripBookingRepository2.findById(tripId).get();
		trip.setStatus(TripStatus.CANCELED);
		trip.getCustomer().getTripBookingList().remove(trip);
		trip.getDriver().getTripBookingList().remove(trip);
		trip.setBill(0);
		tripBookingRepository2.save(trip);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking trip = tripBookingRepository2.findById(tripId).get();
		trip.setBill(trip.getDistanceInKm() * trip.getDriver().getCab().getPerKmRate());
		trip.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(trip);
	}
}
