package servlets;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import com.bittercode.constant.BookStoreConstants;
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
public class AddBookServlet extends HttpServlet {
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

        String bName = req.getParameter(BooksDBConstants.COLUMN_NAME);
        RequestDispatcher rd = req.getRequestDispatcher("SellerHome.html");
        rd.include(req, res);
        StoreUtil.setActiveTab(pw, "addbook");
        pw.println("<div class='container my-2'>");
        
        if(bName == null || bName.isBlank()) {
            //render the add book form;
            showAddBookForm(pw);
            return;
        } //else process the add book
        
        try {
            String uniqueID = UUID.randomUUID().toString();
            String bCode = uniqueID;
            String bAuthor = req.getParameter(BooksDBConstants.COLUMN_AUTHOR);
            double bPrice = Double.parseDouble(req.getParameter(BooksDBConstants.COLUMN_PRICE));
            int bQty = Integer.parseInt(req.getParameter(BooksDBConstants.COLUMN_QUANTITY));
            
            // Handle image upload
            String imagePath = handleImageUpload(req, bCode);
            
            Book book = new Book(bCode, bName, bAuthor, bPrice, bQty);
            book.setImagePath(imagePath); // Assuming you have added imagePath field in Book model
            
            String message = bookService.addBook(book);
            if ("SUCCESS".equalsIgnoreCase(message)) {
                pw.println(
                        "<div class='alert alert-success'>Book Added Successfully!<br/>Book Code: " + bCode + "<br/>Add More Books</div>");
            } else {
                pw.println("<div class='alert alert-danger'>Failed to Add Books! " + message + "</div>");
            }
        } catch (Exception e) {
            e.printStackTrace();
            pw.println("<div class='alert alert-danger'>Failed to Add Books! " + e.getMessage() + "</div>");
        }
        pw.println("</div>");
    }
    
    private String handleImageUpload(HttpServletRequest req, String bookCode) throws IOException, ServletException {
        String uploadPath = getServletContext().getRealPath("") + File.separator + UPLOAD_DIR;
        
        // Create directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }
        
        Part filePart = req.getPart("bookImage");
        String fileName = null;
        
        if (filePart != null && filePart.getSize() > 0) {
            // Get original file name
            String originalFileName = getFileName(filePart);
            String fileExtension = "";
            
            // Extract file extension
            if (originalFileName.contains(".")) {
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
        String[] tokens = contentDisposition.split(";");
        for (String token : tokens) {
            if (token.trim().startsWith("filename")) {
                return token.substring(token.indexOf("=") + 2, token.length() - 1);
            }
        }
        return null;
    }
    
    private static void showAddBookForm(PrintWriter pw) {
        String form = "<div class='container mt-5'>\r\n"
                + "    <div class='row justify-content-center'>\r\n"
                + "        <div class='col-md-6'>\r\n"
                + "            <div class='card'>\r\n"
                + "                <div class='card-header bg-primary text-white'>\r\n"
                + "                    <h4 class='mb-0'>Add New Book</h4>\r\n"
                + "                </div>\r\n"
                + "                <div class='card-body'>\r\n"
                + "                    <form action='addbook' method='post' enctype='multipart/form-data'>\r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookName'>Book Name *</label>\r\n"
                + "                            <input type='text' class='form-control' name='name' id='bookName' \r\n"
                + "                                   placeholder='Enter Book Name' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookAuthor'>Author Name *</label>\r\n"
                + "                            <input type='text' class='form-control' name='author' id='bookAuthor' \r\n"
                + "                                   placeholder='Enter Author Name' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookPrice'>Price (₹) *</label>\r\n"
                + "                            <input type='number' class='form-control' name='price' id='bookPrice' \r\n"
                + "                                   placeholder='Enter Book Price' step='0.01' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookQuantity'>Quantity *</label>\r\n"
                + "                            <input type='number' class='form-control' name='quantity' id='bookQuantity' \r\n"
                + "                                   placeholder='Enter Stock Quantity' required>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div class='form-group'>\r\n"
                + "                            <label for='bookImage'>Book Cover Image</label>\r\n"
                + "                            <input type='file' class='form-control-file' name='bookImage' id='bookImage' \r\n"
                + "                                   accept='image/jpeg,image/png,image/jpg'>\r\n"
                + "                            <small class='form-text text-muted'>\r\n"
                + "                                Supported formats: JPG, PNG (Max size: 5MB)\r\n"
                + "                            </small>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <div id='imagePreview' class='mt-2 mb-3' style='display:none;'>\r\n"
                + "                            <img id='previewImg' src='#' alt='Preview' style='max-width: 150px; max-height: 150px;'>\r\n"
                + "                        </div>\r\n"
                + "                        \r\n"
                + "                        <button type='submit' class='btn btn-success btn-block'>\r\n"
                + "                            <i class='fas fa-plus'></i> Add Book\r\n"
                + "                        </button>\r\n"
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
}