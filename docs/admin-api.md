# Quản trị tài khoản và bảo mật phiên

API quản trị dùng access token có authority `ADMIN` trong header
`Authorization: Bearer <accessToken>`. Response giữ cấu trúc `RestData` của dự án;
DTO người dùng không chứa mật khẩu hoặc token.

| Method | Endpoint | Chức năng |
| --- | --- | --- |
| GET | `/api/v1/admin/users?page=0&size=20` | Tìm kiếm, phân trang người dùng chưa xóa |
| GET | `/api/v1/admin/users/{userId}` | Chi tiết tài khoản chưa xóa |
| PATCH | `/api/v1/admin/users/{userId}/lock` | Khóa tài khoản, ghi nhật ký |
| PATCH | `/api/v1/admin/users/{userId}/unlock` | Mở khóa tài khoản, ghi nhật ký |
| GET | `/api/v1/admin/users/{userId}/actions?page=0&size=20` | Lịch sử quản trị, mới nhất trước |

Danh sách hỗ trợ `keyword` (username, email, điện thoại, họ tên), `sortBy`
(`firstName`, `lastName`, `lastModifiedDate`, mặc định `createdDate`) và
`isAscending` (mặc định `false`). API mới dùng `page` từ 0 ở cả request và
`data.meta.pageNum`, `size` mặc định 20, giới hạn 1–100.

Body của khóa/mở khóa:

```json
{
  "reason": "Đã xác minh hành vi spam",
  "reportId": "<ID báo cáo liên quan, có thể bỏ trường này>"
}
```

`reason` bắt buộc, không chỉ chứa khoảng trắng, tối đa 1000 ký tự. Nếu cung cấp
`reportId`, báo cáo phải tồn tại và phải phản ánh chính tài khoản bị tác động.
Việc gắn báo cáo chưa tự động thay đổi trạng thái xử lý của báo cáo.

Chỉ chuyển `ACTIVE → LOCKED` hoặc `LOCKED → ACTIVE`. Tài khoản admin, tài khoản
chưa xác thực và tài khoản đã xóa không được khóa/mở khóa. Thao tác lặp lại cùng
trạng thái thành công nhưng không thêm nhật ký trùng. Mở khóa không khôi phục
hoặc thay đổi hồ sơ, điểm, lịch hẹn hay trạng thái buddy.

Nhật ký lưu admin thực hiện, người dùng bị tác động, `LOCK`/`UNLOCK`, trạng thái
trước/sau, thời điểm, lý do và báo cáo liên quan. Backend lấy admin từ phiên đăng
nhập, không nhận actor ID từ client. Đổi trạng thái và ghi nhật ký cùng một
transaction; nếu ghi nhật ký thất bại thì trạng thái không đổi. Khóa hàng người
dùng khi cập nhật giúp hai thao tác đồng thời không ghi nhận cùng một chuyển
trạng thái nhiều lần. Lịch sử vẫn đọc được khi tài khoản bị xóa mềm.

`admin_action_logs` là bảng mới, được Hibernate tạo theo cấu hình
`spring.jpa.hibernate.ddl-auto=update` hiện có. Nếu môi trường dùng `validate`
hoặc tắt DDL tự động, cần tạo bảng và index tương ứng với entity `AdminActionLog`
trước khi triển khai. Không có thao tác xóa hay sửa lịch sử qua API.

## Tương thích API cũ

Các endpoint `/api/v1/user`, `/api/v1/user/{userId}` và
`/api/v1/user/{userId}/lock|unlock` tiếp tục tồn tại và yêu cầu `ADMIN`.
Danh sách cũ giữ `pageNum` từ 1 và `pageSize`.

Khóa/mở khóa cũ có thể nhận cùng body mới. Nếu client cũ không gửi body, backend
ghi rõ lý do `No reason supplied through legacy API`; không giả định một lý do
nghiệp vụ. Client mới nên dùng `/admin/users` để bắt buộc nhập lý do.
`GET /api/v1/user/current` vẫn dùng cho mọi tài khoản đã đăng nhập; hồ sơ công
khai dùng `GET /api/v1/profile/{userId}`.

## Token và trạng thái khóa

- Kiểm tra trạng thái từ database khi đăng nhập, refresh và xác thực HTTP/WS.
- Refresh token dùng một lần: Redis kiểm tra blacklist, quyền sở hữu và tiêu thụ
  token nguyên tử bằng Lua. Trong các request refresh đồng thời, chỉ một request
  có thể đổi token cũ thành công. Token refresh được lưu trước đây vẫn được nhận
  nếu hợp lệ, đúng chủ và chưa bị thu hồi.
- Đăng xuất xóa khóa token đang hoạt động và ghi blacklist cho đến khi token hết
  hạn. Cả access và refresh token của lần đăng xuất bị thu hồi. Không cho trộn
  access token của tài khoản A với refresh token của tài khoản B.
- Chỉ access token được dùng để gọi HTTP hoặc xác thực WebSocket; refresh token
  không thể dùng thay access token.
- Token chưa hết hạn có thể dùng lại sau khi tài khoản được mở khóa, nếu token
  chưa bị đăng xuất/thu hồi. Đây là chính sách hiện tại, không phải thu hồi vĩnh
  viễn toàn bộ token khi khóa.

## WebSocket

Endpoint SockJS `/ws` hỗ trợ token qua query `token` (client trình duyệt hiện có)
hoặc header `Authorization: Bearer ...`. `/ws/info` được phép truy cập để SockJS
khởi tạo; handshake và STOMP bắt buộc xác thực. Origin được giới hạn theo các
website và cổng local đã có trong CORS của dự án.

STOMP `CONNECT` có thể gửi access token trong header `Authorization`, nhưng
phải cùng tài khoản với handshake. Token được kiểm tra chữ ký, hạn sử dụng,
loại access, blacklist và trạng thái tài khoản. Các frame tiếp theo và tin nhắn
outbound cũng được kiểm tra lại; đăng xuất hoặc khóa tài khoản sẽ chặn phiên
đang kết nối. Phiên nhàn rỗi được đóng khi có hoạt động/tin gửi tới, không có
cơ chế quét để đóng tức thì mọi kết nối.

Chỉ cho phép:

- `SEND`: `/app/chat.send`, `/app/chat.read`, `/app/chat.typing`. Các nghiệp vụ
  này yêu cầu người dùng đang là thành viên hội thoại.
- `SUBSCRIBE`: `/topic/conversation.{conversationId}` khi là thành viên chưa
  rời hội thoại; `/user/queue/notifications` và `/user/queue/friendship` là các
  queue cá nhân qua user-destination resolver của Spring.

Không cho gửi trực tiếp vào broker `/topic`/`/queue`, đăng ký queue thô hoặc
queue cá nhân của người khác. Kiểm tra lại thành viên trước khi chuyển tin hội
thoại tới subscriber, kể cả khi thành viên đã rời sau lúc đăng ký.

## Kiểm thử

Các test admin dùng database H2, test JWT thật và mock dịch vụ bên ngoài.
`WebSocketProtocolTest` khởi chạy Tomcat và kết nối SockJS/STOMP thật trên máy
kiểm thử. Test rate limit không nằm trong phạm vi bộ kiểm thử này.

```bash
mvn '-Dtest=org.hit.chiikaiwabe.admin.*Test' test
```

Để chạy thêm test vòng đời token với Redis thật, dùng một Redis **riêng cho
kiểm thử** ở local rồi truyền cổng tương ứng:

```bash
mvn '-Dtest=org.hit.chiikaiwabe.admin.*Test' -Dtest.redis.port=16379 test
```

Nếu không cung cấp `test.redis.port`, nhóm `TokenLifecycleRedisTest` được bỏ
qua. Không dùng Redis của server cho các test này. Các test bao gồm refresh
sau logout, blacklist cũ, refresh đồng thời, phân quyền admin, pagination,
rollback nhật ký, liên kết báo cáo, token trước khi khóa, hạn token, quyền
subscribe và truyền tin qua kết nối WebSocket thực.

Workflow `.github/workflows/test-admin.yml` chạy bộ test cùng Redis riêng khi
push nhánh `feature/admin_function` hoặc mở/cập nhật PR vào `main`. Workflow này
không triển khai lên server.
