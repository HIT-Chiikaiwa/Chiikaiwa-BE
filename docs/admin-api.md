# Admin: xem và khóa tài khoản

Đã kiểm tra các nhánh `main`, `dev`, `feature/admin_function`,
`feature/google-auth`, `feature/history_Notification`, `refactor/radar-function`:
chưa có chức năng quản trị khóa tài khoản. `UserBlock` là chặn giữa người dùng.

Các API dưới đây yêu cầu access token của tài khoản có authority `ADMIN`:

| Method | Endpoint | Chức năng |
| --- | --- | --- |
| GET | `/api/v1/user` | Danh sách người dùng chưa xóa, tìm kiếm và phân trang |
| GET | `/api/v1/user/{userId}` | Chi tiết người dùng chưa xóa |
| PATCH | `/api/v1/user/{userId}/lock` | Khóa tài khoản |
| PATCH | `/api/v1/user/{userId}/unlock` | Mở khóa tài khoản |

Dùng header `Authorization: Bearer <accessToken>`. Response giữ cấu trúc
`RestData` hiện có; danh sách trả về `data.meta` và `data.items`, chi tiết dùng
`UserDto` và không có mật khẩu.

Ví dụ tìm kiếm:

```http
GET /api/v1/user?keyword=Nguyen&pageNum=1&pageSize=10&sortBy=firstName&isAscending=true
Authorization: Bearer <accessToken>
```

- `keyword`: tìm theo username, email, số điện thoại hoặc họ tên, không phân biệt
  hoa/thường. Chuỗi rỗng lấy tất cả tài khoản chưa xóa, gồm cả admin.
- `pageNum`: bắt đầu từ 1; `pageSize` mặc định theo `CommonConstant` của dự án.
- `sortBy`: `firstName`, `lastName`, `lastModifiedDate`, mặc định `createdDate`.
- `isAscending`: mặc định `false`.

Khóa/mở khóa không cần request body. Chỉ chuyển `ACTIVE` sang `LOCKED` hoặc
`LOCKED` sang `ACTIVE`. Gọi lặp lại cùng trạng thái vẫn thành công. Không cho
phép khóa/mở khóa tài khoản admin (kể cả bản thân), tài khoản chưa xác thực hay
đã xóa. Không thay đổi dữ liệu hồ sơ, điểm hoặc lịch sử của người dùng.

Trạng thái khóa được kiểm tra từ database khi đăng nhập bằng mật khẩu/Google,
refresh token và khi xác thực mỗi HTTP request bằng token đã cấp. WebSocket
kiểm tra khi handshake, CONNECT, SEND, SUBSCRIBE và trước khi chuyển tin nhắn
tới các phiên đã đăng ký; phiên bị khóa bị từ chối nhận tin và đóng kết nối khi
có tin gửi tới. Kết nối đang nhàn rỗi không bị đóng ngay lúc admin bấm khóa.
Token chưa hết hạn có thể dùng lại sau khi mở khóa.

`GET /api/v1/user/current` vẫn dành cho mọi tài khoản đã đăng nhập.
Người dùng thường xem hồ sơ công khai qua `GET /api/v1/profile/{userId}`.
Các API danh sách, chi tiết quản trị và khóa/mở khóa trả 403 nếu không có quyền
`ADMIN`; ID không tồn tại/đã xóa trả 404; tài khoản chưa xác thực trả 400 khi
khóa/mở khóa.

Tài khoản admin khởi tạo từ `ADMIN_USERNAME`, `ADMIN_PASSWORD` có thể đăng nhập
qua `POST /api/v1/auth/login` với username trong trường `email` hiện có:

```json
{"email":"<ADMIN_USERNAME>","password":"<ADMIN_PASSWORD>"}
```

Kiểm thử riêng chức năng admin (không cần PostgreSQL, Redis hay Firebase thật):

```bash
mvn '-Dtest=org.hit.chiikaiwabe.admin.*Test' test
```

Kiểm thử repository dùng H2 trong bộ nhớ; các kiểm thử xác thực dùng mock dịch
vụ ngoài. Khi triển khai vẫn cần kiểm tra tích hợp với PostgreSQL/Redis và các
client thực tế.
