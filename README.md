# ![Icon](https://img.icons8.com/?size=35&id=104233&format=png&color=000000) Jobhunter
Dự án này được code bằng Java (dự án jobHunter) và độc quyền thuộc về tác giả Hoàng Tấn Dũng

## ![Icon](https://img.icons8.com/?size=20&id=21838&format=png&color=000000) Về tác giả
Mọi thông tin về Tác giả Hoàng Tấn Dũng, các bạn có thể tìm kiếm tại đây:

Youtube “Hoàng Tấn Dũng” : https://www.youtube.com/@crushoicaucutevai7460/featured

Fanpage “Hoàng Tấn Dũng” : https://www.facebook.com/cotem.khong.90?locale=vi_VN


## ![Icon](https://img.icons8.com/?size=25&id=43259&format=png&color=000000) Công nghệ sử dụng và tổng quan dự án

- **Java Spring Boot**: Framework mạnh mẽ để xây dựng backend.

  - **Spring Security**: Đảm bảo bảo mật cho ứng dụng với các tính năng xác thực và phân quyền.

  - **Spring Data JPA**: Quản lý cơ sở dữ liệu dễ dàng thông qua ORM Hibernate.

  - **Spring MVC**: Tách biệt logic và giao diện, áp dụng kiến trúc MVC chuẩn.

- **MySQL**: Cơ sở dữ liệu quan hệ để lưu trữ thông tin người dùng, sản phẩm và đơn hàng.

- **Phân trang**: Giúp tối ưu hóa hiệu năng khi hiển thị danh sách dữ liệu lớn.

- **JSON Web Token**: Giúp bảo mật và quản lý thông tin.

- **Gửi email**: Giúp gửi các job đến người xin việc.

## ![Icon](https://img.icons8.com/?size=30&id=111139&format=png&color=000000) Chức năng chính

### **1. Đăng ký, đăng nhập và quản lý phiên**

- Người dùng có thể đăng ký và đăng nhập tài khoản ( `Xác thực với JWT` )

- Phiên đăng nhập được duy trì để bảo mật và thuận tiện

- Khi chưa đăng nhập, người dùng không thể rải CV vào công ty

### **2. Quản lý phân quyền**
- **Admin**:

  - Toàn quyền quản lý ứng dụng

  - Thực hiện CRUD user, company, role, job, permission, resume

  - Phân quyền theo role và permission (`admin CRUD các role và thêm vào mỗi role các permission khác nhau để phân quyền`)

  - Có khả năng upload file (ví dụ: pdf)

- **User**:

  - Xem công ty, xem job

  - rải CV

  - có thể Thực hiện CRUD user, company, role, job, permission, resume nếu được admin cho phép

- **HR**:

  - xem CV của công ty mình quản lí và cập nhật trạng thái

  - có thể Thực hiện CRUD user, company, role, job, permission, resume nếu được admin cho phép

### **3. Tìm kiếm và lọc công ty, kĩ năng công việc**

- Lọc kĩ năng công việc theo một hoặc nhiều tiêu chí cùng lúc (ví dụ: Java, php, node js)

### **4. Quản lý công việc**

- Người dùng xem những công ty minh đã rải CV.

- HR cập nhật trạng thái CV

### **5. Tối ưu hiệu năng**

- Chức năng phân trang được triển khai cho các danh sách lớn (ví dụ: danh sách công ty, công việc)

## ![Icon](https://img.icons8.com/?size=30&id=108805&format=png&color=000000) Cách chạy ứng dụng

1. **Cài đặt yêu cầu hệ thống**:

   - **Java 17+**.

   - **MySQL 8+**.

2. **Thiết lập cơ sở dữ liệu**:

   - Import các file SQL mẫu để khởi tạo database.

   - Cấu hình thông tin kết nối cơ sở dữ liệu trong file `application.properties`.

3. **Chạy ứng dụng**:

   - Build ứng dụng bằng `Gradle`

4. **Truy cập ứng dụng**:

   - Mở trình duyệt và truy cập: `http://localhost:8080`.

## **Có Thể Mở rộng**

- Triển khai lên server thực tế để ứng dụng có thể được sử dụng trong môi trường production.

- Cải thiện giao diện người dùng với các framework hiện đại như Bootstrap hoặc TailwindCSS.

## ![Icon](https://img.icons8.com/?size=30&id=9YgKo9PXNHu4&format=png&color=000000) Ảnh chụp dự án

### 1. `Home page`


![Home page](https://github.com/dung11122005/image_github/blob/master/jubhunter/homepage1.png)
![Home page](https://github.com/dung11122005/image_github/blob/master/jubhunter/homepage2.png)


### 2. Trang `công ty`


![Trang công ty](https://github.com/dung11122005/image_github/blob/master/jubhunter/page_company.png)


### 3. Trang `công việc`


![Trang công việc](https://github.com/dung11122005/image_github/blob/master/jubhunter/page_job.png)


### 4. Trang `chi tiết công ty`


![Trang chi tiết công ty](https://github.com/dung11122005/image_github/blob/master/jubhunter/detail_company.png)


### 5. Trang `chi tiết công việc`


![Trang chi tiết công ty](https://github.com/dung11122005/image_github/blob/master/jubhunter/detail_job.png)


### 6. `Search` theo 1 hoặc nhiều `tiêu chí`


![Search](https://github.com/dung11122005/image_github/blob/master/jubhunter/search.png)


### 7. `JSON Web Token`


![Trang chi tiết công ty](https://github.com/dung11122005/image_github/blob/master/jubhunter/access_token.png)


### 8. Xem `công ty đã gửi CV`


![Xem công ty đã gửi CV](https://github.com/dung11122005/image_github/blob/master/jubhunter/xem_company_gui_CV.png)


### 9. `Xem CV`


![Xem CV](https://github.com/dung11122005/image_github/blob/master/jubhunter/xem_cv.png)


### 10. `Gửi email`


![Gửi email](https://github.com/dung11122005/image_github/blob/master/jubhunter/send_email.png)


### 11. `Upload file`


![Upload file](https://github.com/dung11122005/image_github/blob/master/jubhunter/upload_file.png)


### 12. Trang `admin`


![Trang admin](https://github.com/dung11122005/image_github/blob/master/jubhunter/adminpage.png)


### 13. Trang `Hr`


![Trang Hr](https://github.com/dung11122005/image_github/blob/master/jubhunter/page_hr.png)


### 14. `Phân quyền` theo role( `admin CRUD các role và thêm vào mỗi role các permission khác nhau để phân quyền` )


![Phân quyền theo role](https://github.com/dung11122005/image_github/blob/master/jubhunter/phan_quyen.png)


### 15. `Permission`


![Permission](https://github.com/dung11122005/image_github/blob/master/jubhunter/permission.png)


### 16. Trang `login` và `đăng kí`

![Trang login](https://github.com/dung11122005/image_github/blob/master/jubhunter/login.png)
![Trang login](https://github.com/dung11122005/image_github/blob/master/jubhunter/dang_ki.png)
