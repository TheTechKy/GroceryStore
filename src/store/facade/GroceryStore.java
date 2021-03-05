package store.facade;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import store.entities.Item;
import store.entities.Member;
import store.entities.Order;
import store.entities.Product;
import store.entities.Transaction;

/**
 * Class GroceryStore is the facade of the application. It acts as the (safe)
 * middle-man between the user interface and the business logic of the grocery
 * store.
 * 
 * @author
 */
public class GroceryStore implements Serializable {

	private static final long serialVersionUID = 1L;
	private static GroceryStore singleton;
	// this class builds and maintains three essential lists: membersList,
	// productsList, and ordersList
	private MembersList membersList = new MembersList();
	private ProductsList productsList = new ProductsList();
	private OrdersList ordersList = new OrdersList();
	private static Transaction checkOut = null;
	private static boolean checkOutOpen = false;
	private static String checkOutMemberId;

	// ------------------------MembersList Class---------------------------------
	/**
	 * Inner class of the GroceryStore. Only GroceryStore can use its methods and
	 * manipulate the list of members.
	 * 
	 * @author
	 *
	 */
	private class MembersList implements Iterable<Member>, Serializable {

		private static final long serialVersionUID = 1L;
		private ArrayList<Member> members = new ArrayList<Member>();

		/**
		 * Adds a new member to the list.
		 * 
		 * @param member - the Member being added
		 * @return the new member's ID
		 */
		public String addMember(Member member) {
			members.add(member);
			return members.get(members.size() - 1).getId();
		}

		/**
		 * This method removes a single member from the members list array.
		 * 
		 * @param id - the unique ID of the member to be removed
		 * @return TRUE if the member was removed, FALSE if the member was not removed
		 */
		public boolean removeMember(String id) {
			for (Member member : members) {
				if (member.getId().equalsIgnoreCase(id)) {
					members.remove(member);
					return true;
				}
			}
			return false;
		}

		/**
		 * Searches for a member with a particular ID.
		 * 
		 * @param id - the ID of the member searched for
		 * @return Member object if found, null if not found
		 */
		public Member search(String id) {
			for (Iterator<Member> iterator = members.iterator(); iterator.hasNext();) {
				Member member = iterator.next();
				if (member.getId().equalsIgnoreCase(id)) {
					return member;
				}
			}
			return null;
		}

		/**
		 * Gets a list of all members.
		 * 
		 * @return an iterator to the list of members
		 */
		public Iterator<Member> iterator() {
			return members.iterator();
		}

	}

	// ------------------------ProductsList Class---------------------------------
	/**
	 * Inner class of the GroceryStore. Only GroceryStore can use its methods and
	 * manipulate the list of products.
	 * 
	 * @author
	 *
	 */
	private class ProductsList implements Iterable<Product>, Serializable {

		private static final long serialVersionUID = 1L;
		private ArrayList<Product> products = new ArrayList<Product>();

		/**
		 * Adds a new product to the list.
		 * 
		 * @param product - the Product being added
		 * @return TRUE if successfully added, FALSE if not added
		 */
		public boolean addProduct(Product product) {
			return products.add(product);
		}

		/**
		 * Searches for a product with a particular ID.
		 * 
		 * @param id - the ID of the product searched for
		 * @return Product object if found, null if not found
		 */
		public Product search(String id) {
			for (Iterator<Product> iterator = products.iterator(); iterator.hasNext();) {
				Product product = iterator.next();
				if (product.getId().equalsIgnoreCase(id)) {
					return product;
				}
			}
			return null;
		}

		/**
		 * Gets a list of all products.
		 * 
		 * @return an iterator to the list of products
		 */
		public Iterator<Product> iterator() {
			return products.iterator();
		}

	}

	// ------------------------OrdersList Class---------------------------------
	/**
	 * Inner class of the GroceryStore. Only GroceryStore can use its methods and
	 * manipulate the list of orders.
	 * 
	 * @author
	 *
	 */
	private class OrdersList implements Iterable<Order>, Serializable {

		private static final long serialVersionUID = 1L;
		private ArrayList<Order> orders = new ArrayList<Order>();

		/**
		 * Adds a new order to the list. There is only one product per order.
		 * 
		 * @param order - the Order being added
		 * @return the new order number
		 */
		public String addOrder(Order order) {
			orders.add(order);
			return orders.get(orders.size() - 1).getOrderNumber();
		}

		/**
		 * Gets a list of outstanding (not yet fulfilled) orders.
		 * 
		 * @return an iterator to the list of outstanding orders
		 */
		public Iterator<Order> outstanding() {
			ArrayList<Order> output = new ArrayList<Order>();
			for (Iterator<Order> iterator = orders.iterator(); iterator.hasNext();) {
				Order order = iterator.next();
				if (!order.isFulfilled()) {
					output.add(order);
				}
			}
			return output.iterator();
		}

		/**
		 * Gets a list of all orders.
		 * 
		 * @return an iterator to the list of orders
		 */
		public Iterator<Order> iterator() {
			return orders.iterator();
		}

	}

	// ------------------------CheckOut Class---------------------------------
	/**
	 * Inner class of the GroceryStore. It's a public class so other entities than
	 * GroceryStore can use it.
	 * 
	 * @author
	 *
	 */
	public class CheckOut {
		// This class is set inside the GorceryStore because it really is the
		// GroceryStore's subject. It's a separate class, not just methods, because its
		// methods have a specific function pertaining to a checkout logic only. It uses
		// several static fields from the GroceryStore that need not be serialized
		// because they are only useful during the execution of the check out
		// functionality. It's set up in a similar way as a singleton: just one instance
		// of the Transaction field it uses (checkOut) is allowed. Before another
		// checkout can be done, the present one has to be closed or cancelled.

		/**
		 * Opens a new checkout, but only if there isn't one open.
		 * 
		 * @param memberId - ID of the member checking out
		 */
		public CheckOut(String memberId) {
			if (checkOut == null) {
				checkOut = new Transaction();
				checkOutMemberId = memberId;
				checkOutOpen = true;
			}
		}

		/**
		 * Returns the state of the checkout.
		 * 
		 * @return TRUE if checkout is in progress, FALSE if not
		 */
		public boolean isOpen() {
			return checkOutOpen;
		}

		/**
		 * Gets the total of the running checkout.
		 * 
		 * @return checkout total
		 */
		public double getTotalPrice() {
			return checkOut.getTotalPrice();
		}

		/**
		 * Puts an item of a certain quantity on a checkout.
		 * 
		 * @param request - object of the data transfer logic carrying the relevant
		 *                information (product ID and quantity checked out)
		 * @return result code indicating the result of the action
		 */
		public Result addItem(Request request) {
			Result result = new Result();
			Product product = productsList.search(request.getProductId());
			// next if clause is carried out if there is a running checkout AND the product
			// ID is valid AND there is enough quantity of the product on hand
			if (checkOutOpen && product != null && product.getStockOnHand() >= request.getOrderQuantity()) {
				// item is added to checkout
				checkOut.addItem(new Item(product.getName(), product.getId(), request.getOrderQuantity(),
						product.getCurrentPrice()));
				// result fields are filled with relevant information (checked out product's
				// fields and quantity)
				result.setProductFields(product);
				result.setOrderQuantity(request.getOrderQuantity());
				// the stock on hand is updated
				product.setStockOnHand(product.getStockOnHand() - request.getOrderQuantity());
				// the result code is set
				result.setResultCode(Result.ACTION_SUCCESSFUL);
			} else {
				if (!checkOutOpen) {
					result.setResultCode(Result.ACTION_FAILED);
					return result;
				}
				if (product == null) {
					result.setResultCode(Result.INVALID_PRODUCT_ID);
					return result;
				}
				if (product.getStockOnHand() < request.getOrderQuantity()) {
					result.setResultCode(Result.INVALID_ORDER_QUANTITY);
					return result;
				}
			}
			return result;
		}

		/**
		 * Cancels the running checkout without effecting the member's transactions or
		 * stock-on-hand information.
		 * 
		 * @return result code indicating the result of the action
		 */
		public Result cancelCheckOut() {
			Result result = new Result();
			if (checkOutOpen) {
				// running checkout is closed
				checkOutOpen = false;
				// the subtracted quantities of items checked out are returned back to
				// stock-on-hand (the products are "re-stocked")
				for (Iterator<Item> iterator = checkOut.getItems(); iterator.hasNext();) {
					Item item = iterator.next();
					Product product = productsList.search(item.getProductId());
					product.setStockOnHand(product.getStockOnHand() + item.getQuantity());
				}
				// checkout is annulled
				checkOutMemberId = "";
				checkOut = null;
				result.setResultCode(Result.ACTION_SUCCESSFUL);
			} else {
				result.setResultCode(Result.ACTION_FAILED);
			}
			return result;
		}

		/**
		 * Closes checkout with the transaction being recorded in member and low
		 * products reordered.
		 * 
		 * @return iterator on the list of items reordered (stored in an arrayList of
		 *         result (part of data transfer logic))
		 */
		public Iterator<Result> closeCheckOut() {
			ArrayList<Result> list = new ArrayList<Result>();
			if (checkOutOpen) {
				// running checkout is closed
				checkOutOpen = false;
				Member member = membersList.search(checkOutMemberId);
				// new transaction is added to the member
				member.addTransaction(checkOut);
				// for loop is iterating over the list of all items checked out to find out if
				// any product needs to be reordered
				for (Iterator<Item> iterator = checkOut.getItems(); iterator.hasNext();) {
					Item item = iterator.next();
					Product product = productsList.search(item.getProductId());
					Result result = new Result();
					// next if clause is carried out if the product stock is low AND the product
					// doesn't have a pending order
					if (product.getStockOnHand() <= product.getReorderLevel() && !product.isOrdered()) {
						// a new order for a product (from a vendor) is placed
						result.setOrderId(ordersList.addOrder(
								new Order(product.getName(), product.getId(), product.getReorderLevel() * 2)));
						// result fields for this product (for the particular iteration of the for loop)
						// are filled with relevant information (reordered product's fields and quantity
						// ordered)
						result.setProductFields(product);
						result.setOrderQuantity(product.getReorderLevel() * 2);
						// the result code is set
						result.setResultCode(Result.ACTION_SUCCESSFUL);
						// this product is marked as ordered (pending order)
						product.setOrdered(true);
						// this result with fields indicating a reordered product is added to the list
						list.add(result);
					}
				}
				checkOutMemberId = "";
				checkOut = null;
			}
			// and iterator on the created list of reordered products is returned
			return list.iterator();
		}
	}

	// ------------------------GroceryStore Class---------------------------------
	// -----------It's expected to be extended with new functionality.------------
	/**
	 * GroceryStore's constructor. It's a singleton.
	 */
	private GroceryStore() {
	}

	public static GroceryStore instance() {
		if (singleton == null) {
			singleton = new GroceryStore();
		}
		return singleton;
	}

	/**
	 * Used by UI, pre-loaded with request (data transfer logic), carries out the
	 * addMember of the MembersList (inner class).
	 * 
	 * @param request carries relevant member fields
	 * @return result (data transfer logic), filled with member ID and result code
	 */
	public Result addMember(Request request) {
		Result result = new Result();
		String memberId = "";
		// member is added to membersList using MembersList method and request's member
		// fields
		memberId = membersList.addMember(new Member(request.getMemberName(), request.getMemberAddress(),
				request.getMemberPhoneNumber(), request.getMemberDateJoined(), request.getMemberFeePaid()));
		// result is filled with relevant information (member ID and result code)
		result.setMemberId(memberId);
		if (memberId != "") {
			result.setResultCode(Result.ACTION_SUCCESSFUL);
		} else {
			result.setResultCode(Result.ACTION_FAILED);
		}
		return result;
	}

	/**
	 * Used by UI, gets the list of all members on record. (Converts an
	 * Iterator<member> to Iterator<result>.)
	 * 
	 * @return iterator on the list of members
	 */
	public Iterator<Result> getAllMembers() {
		ArrayList<Result> list = new ArrayList<Result>();
		for (Iterator<Member> iterator = membersList.iterator(); iterator.hasNext();) {
			Member member = iterator.next();
			Result result = new Result();
			result.setMemberFields(member);
			list.add(result);
		}
		return list.iterator();
	}

	/**
	 * Validates member ID.
	 * 
	 * @param memberId - ID being validated
	 * @return TRUE if member exists, FALSE if member doesn't exits
	 */
	public boolean validateMemberId(String memberId) {
		return (membersList.search(memberId) != null);
	}

	/**
	 * Validates product ID.
	 * 
	 * @param productId - ID being validated
	 * @return TRUE if product is on record, FALSE if product isn't on record
	 */
	public boolean validateProductId(String productId) {
		return (productsList.search(productId) != null);
	}

}