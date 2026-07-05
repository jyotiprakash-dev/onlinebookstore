package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.util.StoreUtil;

public class ViewBookServlet extends HttpServlet {

    // book service for database operations and logics
    BookService bookService = new BookServiceImpl();

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter pw = res.getWriter();
        res.setContentType("text/html");

        // Check if the customer is logged in, or else return to login page
        if (!StoreUtil.isLoggedIn(UserRole.CUSTOMER, req.getSession())) {
            RequestDispatcher rd = req.getRequestDispatcher("CustomerLogin.html");
            rd.include(req, res);
            pw.println("<table class=\"tab\">客<td>Please Login First to Continue!!</td></tr></table>");
            return;
        }
        try {

            // Read All available books from the database
            List<Book> books = bookService.getAllBooks();

            // Default Page to load data into
            RequestDispatcher rd = req.getRequestDispatcher("CustomerHome.html");
            rd.include(req, res);

            // Set Available Books tab as active
            StoreUtil.setActiveTab(pw, "books");

            // Show the heading for the page
            pw.println("<div id='topmid' style='background-color:grey; padding: 15px;'>"
                    + "<h3 style='display: inline-block;'>Available Books</h3>"
                    + "<form action=\"cart\" method=\"post\" style='float:right; margin-right:20px'>"
                    + "<input type='submit' class=\"btn btn-primary\" name='cart' value='Proceed to Cart'/></form>"
                    + "</div>");
            pw.println("<div class=\"container mt-4\">\r\n"
                    + "        <div class=\"row\">");

            // Add or Remove items from the cart, if requested
            StoreUtil.updateCartItems(req);

            HttpSession session = req.getSession();
            int count = 0;
            for (Book book : books) {
                // Add each book to display as a card
                pw.println(this.addBookToCard(session, book));
                count++;
                // Add a new row after every 3 books
                if (count % 3 == 0 && count < books.size()) {
                    pw.println("</div><div class='row mt-4'>");
                }
            }

            // Checkout Button
            pw.println("</div>"
                    + "<div class='text-center mt-4 mb-4'>"
                    + "<form action=\"cart\" method=\"post\">"
                    + "<input type='submit' class=\"btn btn-success btn-lg\" name='cart' value='Proceed to Checkout'/>"
                    + "</form>"
                    + "</div>");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String addBookToCard(HttpSession session, Book book) {
        String bCode = book.getBarcode();
        int bQty = book.getQuantity();
        String imagePath = book.getImagePath();
        
        // Default image if no image is available
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "default-book.jpg";
        }

        // Quantity of the current book added to the cart
        int cartItemQty = 0;
        if (session.getAttribute("qty_" + bCode) != null) {
            // Quantity of each book in the cart will be added in the session prefixed with
            // 'qty_' following with bookId
            cartItemQty = (int) session.getAttribute("qty_" + bCode);
        }

        // Button To Add/Remove item from the cart
        String button = "";
        if (bQty > 0) {
            // If no items in the cart, show add to cart button
            // If items is added to the cart, then show +, - button to add/remove more items
            if (cartItemQty == 0) {
                button = "<form action=\"viewbook\" method=\"post\">"
                        + "<input type='hidden' name='selectedBookId' value='" + bCode + "'>"
                        + "<input type='hidden' name='qty_" + bCode + "' value='1'/>"
                        + "<button type='submit' name='addToCart' class='btn btn-primary btn-sm'>Add to Cart</button>"
                        + "</form>";
            } else {
                button = "<div class='btn-group' role='group'>"
                        + "<form method='post' action='cart' style='display: inline-block;'>"
                        + "<input type='hidden' name='selectedBookId' value='" + bCode + "'/>"
                        + "<button type='submit' name='removeFromCart' class='btn btn-danger btn-sm'>-</button>"
                        + "</form>"
                        + "<span class='mx-2' style='font-weight: bold; padding: 5px 10px;'>" + cartItemQty + "</span>"
                        + "<form method='post' action='cart' style='display: inline-block;'>"
                        + "<input type='hidden' name='selectedBookId' value='" + bCode + "'/>"
                        + "<button type='submit' name='addToCart' class='btn btn-success btn-sm'>+</button>"
                        + "</form>"
                        + "</div>";
            }
        } else {
            // If available Quantity is zero, show out of stock button
            button = "<button class='btn btn-danger btn-sm' disabled>Out of Stock</button>";
        }

        // Bootstrap card with book image
        return "<div class='col-md-4 mb-4'>\r\n"
                + "    <div class='card h-100 shadow-sm'>\r\n"
                + "        <div class='row no-gutters'>\r\n"
                + "            <div class='col-md-4'>\r\n"
                + "                <img src='" + imagePath + "' class='card-img' alt='" + book.getName() + "' \r\n"
                + "                     style='padding-left:10px; height: 100px; width: 100px; object-fit: cover;'>\r\n"
                + "            </div>\r\n"
                + "            <div class='col-md-8'>\r\n"
                + "                <div class='card-body'>\r\n"
                + "                    <h5 class='card-title text-success'>" + escapeHtml(book.getName()) + "</h5>\r\n"
                + "                    <p class='card-text'>\r\n"
                + "                        <strong>Author:</strong> <span class='text-primary'>" + escapeHtml(book.getAuthor()) + "</span><br>\r\n"
                + "                        <strong>Book Code:</strong> " + bCode + "<br>\r\n"
                + "                        <strong>Price:</strong> <span style='color: green; font-weight: bold;'>₹ " + String.format("%.2f", book.getPrice()) + "</span><br>\r\n"
                + "                        <strong>Availability:</strong> " + (bQty > 0 ? 
                                (bQty < 20 ? "<span class='text-warning'>Only " + bQty + " left!</span>" : 
                                 "<span class='text-success'>In Stock</span>") : 
                                 "<span class='text-danger'>Out of Stock</span>") + "<br>\r\n"
                + "                    </p>\r\n"
                + "                    <div class='mt-2'>\r\n"
                + button + "\r\n"
                + "                    </div>\r\n"
                + "                </div>\r\n"
                + "            </div>\r\n"
                + "        </div>\r\n"
                + "    </div>\r\n"
                + "</div>";
    }
    
    // Helper method to escape HTML special characters
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}