import SwiftUI

struct HomeView: View {
    let onOpenSettings: () -> Void

    var body: some View {
        Text("首頁")
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .navigationTitle("首頁")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("設定", systemImage: "gear", action: onOpenSettings)
                        .labelStyle(.iconOnly)
                }
            }
    }
}
