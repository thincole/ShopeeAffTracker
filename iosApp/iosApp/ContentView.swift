import SwiftUI

struct ContentView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "cart.fill")
                .resizable()
                .frame(width: 64, height: 64)
                .foregroundColor(.orange)

            Text("Shopee Chuyển Đổi")
                .font(.largeTitle)
                .bold()

            Text("Version 1.0.2 ▪ Đa Thị Trường")
                .font(.subheadline)
                .foregroundColor(.gray)

            Text("Tác giả: Thinaptm")
                .font(.caption)
                .foregroundColor(.secondary)
        }
        .padding()
    }
}

#Preview {
    ContentView()
}
