package servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.bittercode.constant.BookStoreConstants;
import com.bittercode.constant.ResponseCode;
import com.bittercode.constant.db.BooksDBConstants;
import com.bittercode.model.Book;
import com.bittercode.model.UserRole;
import com.bittercode.service.BookService;
import com.bittercode.service.impl.BookServiceImpl;
import com.bittercode.util.StoreUtil;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024, // 1 MB
    maxFileSize = 1024 * 1024 * 5,   // 5 MB
    maxRequestSize = 1024 * 1024 * 10 // 10 MB
)
public class UpdateBookServlet extends HttpServlet {
    BookService bookService = new BookServiceImpl();
    
    // Directory to store uploaded book images
    private static final String UPLOAD_DIR = "book-images";

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter pw = res.getWriter();
        res.setContentType(BookStoreConstants.CONTENT_TYPE_TEXT_HTML);

        if (!StoreUtil.isLoggedIn(UserRole.SELLER, req.getSession())) {
            RequestDispatcher rd = req.getRequestDispatcher("SellerLogin.html");
            rd.include(req, res);
            pw.println("<table class=\"tab\">客<td>Please Login First to Continue!!</td>客</table>");
            return;
        }

        RequestDispatcher rd = req.getRequestDispatcher("SellerHome.html");
        rd.include(req, res);
        StoreUtil.setActiveTab(pw, "storebooks");
        pw.println("<div class='container my-2'>");

        try {
            if (req.getParameter("updateFormSubmitted") != null) {
                String bName = req.getParameter(BooksDBConstants.COLUMN_NAME);
                String bCode = req.getParameter(BooksDBConstants.COLUMN_BARCODE);
                String bAuthor = req.getParameter(BooksDBConstants.COLUMN_AUTHOR);
                double bPrice = Double.parseDouble(req.getParameter(BooksDBConstants.COLUMN_PRICE));
                int bQty = Integer.parseInt(req.getParameter(BooksDBConstants.COLUMN_QUANTITY));
                
                // Handle image upload
                String imagePath = handleImageUpload(req, bCode);
                
                Book book = new Book(bCode, bName, bAuthor, bPrice, bQty);
                
                // If a new image was uploaded, set it
                if (imagePath != null) {
                    book.setImagePath(imagePath);
                } else {
                    // Keep existing image
                    Book existingBook = bookService.getBookById(bCode);
                    book.setImagePath(existingBook.getImagePath());
                }
                
                String message = bookService.updateBook(book);
                
                if (ResponseCode.SUCCESS.name().equalsIgnoreCase(message)) {
                    pw.println(
                            "<div class='alert alert-success alert-dismissible fade show' role='alert'>" +
                            "Book Detail Updated Successfully!" +
                            "<button type='button' class='close' data-dismiss='alert' aria-label='Close'>" +
                            "<span aria-hidden='true'>&times;</span></button></div>");
                } else {
                    pw.println("<div class='alert alert-danger'>Failed to Update Book!! " + message + "</div>");
                }
                return;
            }

            String bookId = req.getParameter("bookId");

            if (bookId != null) {
                Book book = bookService.getBookById(bookId);
                showUpdateBookForm(pw, book);
            }

        } catch (Exception e) {
            e.printStackTrace();
            pw.println("<div class='alert alert-danger'>Failed to Load Book data!! " + e.getMessage() + "</div>");
        }
        pw.println("</div>");
    }
    
    private String handleImageUpload(HttpServletRequest req, String bookCode) throws IOException, ServletException {
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        
        // Create directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        
        Part filePart = req.getPart("bookImage");
        String fileName = null;
        
        if (filePart != null && filePart.getSize() > 0) {
            // Check if there's an existing image to delete
            Book existingBook = bookService.getBookById(bookCode);
            if (existingBook != null && existingBook.getImagePath() != null) {
                String oldImagePath = getServletContext().getRealPath("") + File.separator + existingBook.getImagePath();
                File oldImage = new File(oldImagePath);
                if (oldImage.exists()) {
                    oldImage.delete();
                }
            }
            
            // Get original file name
            String originalFileName = getFileName(filePart);
            String fileExtension = "";
            
            // Extract file extension
            if (originalFileName != null && originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            
            // Create unique file name using book code
            fileName = bookCode + fileExtension;
            String filePath = uploadPath + File.separator + fileName;
            
            // Save the file
            filePart.write(filePath);
        }
        
        return fileName != null ? UPLOAD_DIR + "/" + fileName : null;
    }
    
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition != null) {
            String[] tokens = contentDisposition.split(";");
            for (String token : tokens) {
                if (token.trim().startsWith("filename")) {
                    return token.substring(token.indexOf("=") + 2, token.length() - 1);
                }
            }
        }
        return null;
    }
    
    private static void showUpdateBookForm(PrintWriter pw, Book book) {
        String form = "<div class='container mt-5'>\r\n"
                + "    <div class='row justify-content-center'>\r\n"
                + "        <div class='col-md-6'>\r\n"
                + "            <div class='card'>\r\n"
                + "                <div class='card-header bg-warning text-dark'>\r\n"
                + "                    <h4 class='mb-0'>Update Book Details</h4>\r\n"
                + "                </div>\r\n"
                + "                <div class='card-body'>\r\n"
                + "                    <form action='updatebook' method='post' enctype='multipart/form-data'>\r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookCode'>Book Code</label>\r\n"
                + "                            <input type='text' class='form-control' name='barcode' id='bookCode' \r\n"
                + "                                   value='" + book.getBarcode() + "' readonly>\r\n"
                + "                            <small class='form-text text-muted'>Book code cannot be changed</small>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookName'>Book Name *</label>\r\n"
                + "                            <input type='text' class='form-control' name='name' id='bookName' \r\n"
                + "                                   value='" + escapeHtml(book.getName()) + "' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookAuthor'>Author Name *</label>\r\n"
                + "                            <input type='text' class='form-control' name='author' id='bookAuthor' \r\n"
                + "                                   value='" + escapeHtml(book.getAuthor()) + "' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookPrice'>Price (₹) *</label>\r\n"
                + "                            <input type='number' class='form-control' name='price' id='bookPrice' \r\n"
                + "                                   value='" + book.getPrice() + "' step='0.01' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookQuantity'>Quantity *</label>\r\n"
                + "                            <input type='number' class='form-control' name='quantity' id='bookQuantity' \r\n"
                + "                                   value='" + book.getQuantity() + "' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label>Current Book Cover</label><br>\r\n"
                + "                            <div id='currentImage' class='mb-2'>\r\n";
        
        // Display current image if exists
        if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
            form += "                                <img src='" + book.getImagePath() + "' alt='Current Book Cover' \r\n"
                    + "                                     style='max-width: 150px; max-height: 150px; border: 1px solid #ddd; padding: 5px;'>\r\n";
        } else {
            form += "                                <img src='default-book.jpg' alt='No Image' \r\n"
                    + "                                     style='max-width: 150px; max-height: 150px; border: 1px solid #ddd; padding: 5px;'>\r\n";
        }
        
        form += "                            </div>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookImage'>Update Book Cover Image (Optional)</label>\r\n"
                + "                            <input type='file' class='form-control-file' name='bookImage' id='bookImage' \r\n"
                + "                                   accept='image/jpeg,image/png,image/jpg'>\r\n"
                + "                            <small class='form-text text-muted'>\r\n"
                + "                                Leave empty to keep current image. Supported formats: JPG, PNG (Max size: 5MB)\r\n"
                + "                            </small>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div id='imagePreview' class='mt-2 mb-3' style='display:none;'>\r\n"
                + "                            <label>New Image Preview:</label><br>\r\n"
                + "                            <img id='previewImg' src='#' alt='Preview' style='max-width: 150px; max-height: 150px; border: 1px solid #ddd; padding: 5px;'>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <button type='submit' name='updateFormSubmitted' class='btn btn-warning btn-block'>\r\n"
                + "                            <i class='fas fa-edit'></i> Update Book\r\n"
                + "                        </button>\r\n"
                + "                        \r\n"
                + "                        <a href='SellerHome.html' class='btn btn-secondary btn-block mt-2'>\r\n"
                + "                            Cancel\r\n"
                + "                        </a>\r\n"
                + "                    </form>\r\n"
                + "                </div>\r\n"
                + "            </div>\r\n"
                + "        </div>\r\n"
                + "    </div>\r\n"
                + "</div>\r\n"
                + "\r\n"
                + "<script>\r\n"
                + "    // Image preview functionality\r\n"
                + "    document.getElementById('bookImage').addEventListener('change', function(e) {\r\n"
                + "        const file = e.target.files[0];\r\n"
                + "        const preview = document.getElementById('imagePreview');\r\n"
                + "        const previewImg = document.getElementById('previewImg');\r\n"
                + "        \r\n"
                + "        if (file) {\r\n"
                + "            const reader = new FileReader();\r\n"
                + "            reader.onload = function(e) {\r\n"
                + "                previewImg.src = e.target.result;\r\n"
                + "                preview.style.display = 'block';\r\n"
                + "            }\r\n"
                + "            reader.readAsDataURL(file);\r\n"
                + "        } else {\r\n"
                + "            preview.style.display = 'none';\r\n"
                + "            previewImg.src = '#';\r\n"
                + "        }\r\n"
                + "    });\r\n"
                + "</script>";
        
        pw.println(form);
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