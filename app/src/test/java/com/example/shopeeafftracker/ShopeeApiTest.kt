package com.example.shopeeafftracker

import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ShopeeApiTest {

    private val myLines = listOf(
        "MY3001|SPC_F=Rjgn41euz3F9g2n5401Qxb8iPYEuXJAd; REC_T_ID=cd4f392c-9a52-11f1-8dd3-c654079180f1; SPC_ST=dpaY7hwXJmMcv9gwKw2K/25gvwR1jTEcJqgRRnOWLYhglUDmBdZGFJqDjs+Fu3ooTX/26Eec3vHRItvvwPClDf9HJXwT1Ps6fLaCNGynD13uk5+DZGJtf79ybZIzIgmUqXAUX7Ov4f5NefOyJH7pfwQrW4xRM255C2AXWSBbbbBKraYI+JUCXsj8J+g+pwZv3mxRbZExxGhR1rr0WpPmAA==.ABBLB3YltwRcenB75wqTw2AIQ0scOjyW9SNjGH1mGn3Y; SPC_U=16843956456; SPC_R_T_ID=kTa//s0Hhg5z69aWK5dary30hKPYElkWP7YW+V61V70vuxC7WZYw0bCRoJuKsnvf9bPC7G7tDR0FemNjLw8sspw7JyMO74/na2u97okjExSfYeWlGK3E7yPckWR24tl35tQewNitFBz3JAJQtV3wEgjhKYsrH0p044OnGFFkGIk=; SPC_R_T_IV=bWFpZHk0SlppTGZuclNxaw==; SPC_T_ID=kTa//s0Hhg5z69aWK5dary30hKPYElkWP7YW+V61V70vuxC7WZYw0bCRoJuKsnvf9bPC7G7tDR0FemNjLw8sspw7JyMO74/na2u97okjExSfYeWlGK3E7yPckWR24tl35tQewNitFBz3JAJQtV3wEgjhKYsrH0p044OnGFFkGIk=; SPC_T_IV=bWFpZHk0SlppTGZuclNxaw==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3002|SPC_F=162UgF86tyvI4i6iyPw8sZidSbWb9zNG; REC_T_ID=47b39fc2-9a53-11f1-9802-a6ed36ffe0da; SPC_ST=9XcbR3gzMCOtDi/St6I45WfEKzfiB/Ici3FG0QaArHZwGz6rHHjrnHcHLYR6ih6cnIERC02agqbf9HpRkPP9xu/4o/hEzPVxpRa3eac1OOXqHv0U3Ht9hmHUjBxnr4qjiJEPRhDt9BL3iF1KxL5i6UQjI6eBGp00vyCfkecT9ZQLDPb/xCTiwUj03fzvvP56IG2DVjEP7zhaod1q+J1Vew==.ANP7yhnHZ8xALC5cfpzb0TjQimF9SFmqJBJjBLpBBMAU; SPC_U=15912533334; SPC_R_T_ID=2szHUxMn2yw8mu/MS1fVWnyHlZg+qgRy0/74/skF0q6FgVPcpo4GqBJXtkVMvNH+ltCKUywOwdV5jpXaDhzN344ecRfCOfqnXgtiDr/EAYphW+eUE6vLUUj38MopHSp5Rd0aQ9/0Sea8Xeqb7H+/6ccdN34lA77ZaJh5rCLvvpw=; SPC_R_T_IV=VERPaktiSlhxUVhxeFhaVw==; SPC_T_ID=2szHUxMn2yw8mu/MS1fVWnyHlZg+qgRy0/74/skF0q6FgVPcpo4GqBJXtkVMvNH+ltCKUywOwdV5jpXaDhzN344ecRfCOfqnXgtiDr/EAYphW+eUE6vLUUj38MopHSp5Rd0aQ9/0Sea8Xeqb7H+/6ccdN34lA77ZaJh5rCLvvpw=; SPC_T_IV=VERPaktiSlhxUVhxeFhaVw==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3003|SPC_F=MhGbJd08k8eoGeUEyTErNg4RoPhYzlYs; REC_T_ID=04b92e4b-9a54-11f1-a6a3-c2bbb040b79d; SPC_ST=NK3i3+xolMrmCh2K/fMVAwHzDdheY/dilZoj2iC5Ksvl9SFcSk54TywPs9j/Nf2c236Adc1q+vAjc3PKbWBE7mksCnzAz65db96QeN3PFnq/Cktq19KQ7lx1Nkyy9R0R4C97Em+pyNED6ACFGt3RjQpr9VC0m/Gb3HgoEcmrXK2kSLwNA80lrn99K6twvubnc0W8OfxE7GNBCQDMDw18U3h5of5XxChVNwMN2AvO2xQ=.AJI8FWj00EAHeQPpqBSNvjrFSaKKp6q5t6wOppQCXO+W; SPC_U=16830391505; SPC_R_T_ID=CwtBfkpZgfZn+1Iya96wiWz0ra2XVz15iNqzNAnG+gdgNMF9lS3tJKBk8IXpJS1T8jgaEMewuoB1wpOgeToW93ZN5JsD7KLeAv0bJrcf42nFxZQ2az7LyU69RVsv1nPMZBch/JUZBFRf4pXLoPuQqrrLTyO2x2SV8flWVyxZm3A=; SPC_R_T_IV=ZlQxb1FLeDBySTRoQmFrMQ==; SPC_T_ID=CwtBfkpZgfZn+1Iya96wiWz0ra2XVz15iNqzNAnG+gdgNMF9lS3tJKBk8IXpJS1T8jgaEMewuoB1wpOgeToW93ZN5JsD7KLeAv0bJrcf42nFxZQ2az7LyU69RVsv1nPMZBch/JUZBFRf4pXLoPuQqrrLTyO2x2SV8flWVyxZm3A=; SPC_T_IV=ZlQxb1FLeDBySTRoQmFrMQ==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3005|SPC_F=D1OYCsK9xeHvzEnxhMrXr48u7QupcGys; REC_T_ID=c3949d05-9a54-11f1-810b-ba581a293792; SPC_ST=cvQyF4sn1zakcxGXDprPGRG3nDP/CpF+IKrvv9xkB1uDZgBpTATZSpUMQMvJtkNpLNdDRCrrkfUaiyJ04/SErSj1p5x7ehQUPlrcC6fbKwm6iHaDp1lw0lcijXuJ1N0aoH/HT4d9Whv66dUX/h4+q5Q7rsTzpRhpTOinmR4MY7eZL7CLlGWJAyLGlXp4GVG5ImDjDTca8152+SLBGYqVvrdIUW1FcJAMLuciepq/4Cs=.AMg6kb32u9sU89WxnGW0aZAigvlrwVAjccoKEKuxV6FR; SPC_U=13013415265; SPC_R_T_ID=lHgi4q9MzZIt7d7xilpDulo2YUeYdrpMEvbN/vdwjz6PV6F3Ck0nGDjeSu/7RzmVP7FVBIuGc9Il30MXczHJYdT6orQjyA8n+V36sTemYBS8NFm7N2cdAgC6InxiWQR8K7BbYvN7pdtd+RlNaPHj0kzTD1Co1t3JQiAmSbqsykU=; SPC_R_T_IV=MDNlVDNDZkEzTlFtSjdsMQ==; SPC_T_ID=lHgi4q9MzZIt7d7xilpDulo2YUeYdrpMEvbN/vdwjz6PV6F3Ck0nGDjeSu/7RzmVP7FVBIuGc9Il30MXczHJYdT6orQjyA8n+V36sTemYBS8NFm7N2cdAgC6InxiWQR8K7BbYvN7pdtd+RlNaPHj0kzTD1Co1t3JQiAmSbqsykU=; SPC_T_IV=MDNlVDNDZkEzTlFtSjdsMQ==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3008|SPC_F=aE3klb7nCveuNXaWUU7LigpmNNJWwhgC; REC_T_ID=c503eaee-9a55-11f1-b4d0-6e99a057cebf; SPC_ST=Yagf9+M5f8m+BUBHPOqT+5AX7hE//tWaqwiIoTwxGB5G+Bhm6wTMBFxZDxE4F/PjelPC/vZEu/oijfDst5ByAcxsJ1bZ+K/Q9DEw6fpubViVLcbJ46TIglLkZr0cy/3thqIh9L5IdXmtrW3ZMUiuWtat5w8eAgoDhnC/gnbjmaNHzYS5LMzEzkIkyK6rNeIUxrfOMrrb6hZZgiR3wgb5hItPCSAq11/IJ2pWE6JQLto=.AF4EyN4sZbj5SpnusAktj6FqtswWsZHUUcwpYIoiB9ZE; SPC_U=16607362852; SPC_R_T_ID=18IyOabzMcJdU2nGPuU6AyxpSDd6H5GIhbUjib23XSqwRS3c5QCg7hDyjf2pOvM6Re0Zp1YW3iLsq+GDiJ/j0H9PuyWPFpU5Hq1vJ/6s9ojMFipxOBDUwqoeQaGfudXvR02NKxZZu1K6ef6Cx1TtMAci7NjklpD/6qF+PZon1KI=; SPC_R_T_IV=Q2NqWTA0Q2VHR2s0eld2Zw==; SPC_T_ID=18IyOabzMcJdU2nGPuU6AyxpSDd6H5GIhbUjib23XSqwRS3c5QCg7hDyjf2pOvM6Re0Zp1YW3iLsq+GDiJ/j0H9PuyWPFpU5Hq1vJ/6s9ojMFipxOBDUwqoeQaGfudXvR02NKxZZu1K6ef6Cx1TtMAci7NjklpD/6qF+PZon1KI=; SPC_T_IV=Q2NqWTA0Q2VHR2s0eld2Zw==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3009|SPC_F=f6TNTcbbTGgTxLzCx0vhLraa2Zbxinoj; REC_T_ID=2d8dcee1-9afc-11f1-905e-c2d83f31d9d4; SPC_ST=FsNjeolkQ757KeTNPc8ckSmS1IdWyboiqBwhNEcx22TpUZ+4Wg8/jj06jgV+GNrbSS2/PBmlDn7aRPbIxV+/1bTAiAbwYQbfVWsZF89DKyo9bfCp2D9/RFqTJRc/VYgqfhtafKxjFHlll2GyvTViKiShhJ4Qmq79WU9du9DLWUOT4f5a3ibPd62CTSs6SA0aKi1ePYLqfQ98FtIPAhnnzciR7b1qRCJFnTBAn1GEOmM=.ANud7NzkFCTzweaZIY1qMGUtWoHILuDKgECQoF5hNlNM; SPC_U=13220618795; SPC_R_T_ID=nYs/jp63ovaUKJ0ajEPnB1VniAQE9IiaXiuPiAvTRZDlRtLi7R93PznUtKCidbhTQ53fv+HtGIFOGhVV8vzz6IdaBsJ43XmvjbrNeD8FT/ws9MaW26jYdCNGybmYeaJuuVkBhDNv444QjgVV8TZdRLRQvTW4o6tvNIedGzSf5to=; SPC_R_T_IV=VUFSbnRIazRndEZHb01FSQ==; SPC_T_ID=nYs/jp63ovaUKJ0ajEPnB1VniAQE9IiaXiuPiAvTRZDlRtLi7R93PznUtKCidbhTQ53fv+HtGIFOGhVV8vzz6IdaBsJ43XmvjbrNeD8FT/ws9MaW26jYdCNGybmYeaJuuVkBhDNv444QjgVV8TZdRLRQvTW4o6tvNIedGzSf5to=; SPC_T_IV=VUFSbnRIazRndEZHb01FSQ==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3010|SPC_F=3X2unyEVxdj7dpJ6Lyv6OjGMTcfguIP2; REC_T_ID=690053ae-9afc-11f1-b857-1e628a4c836d; SPC_ST=COkDXDS8hjXEUBo/xrMGXu1/PwB7ENT5UDUKAq3UtPSkLE5IulzA0oFs3bK8P60xv8RsAlluu2tE3nbkkwpi9NtCStT/JS36jeIWotgkxlOjsp37Er7+AKkdAT7fgjRo1LyHG7BUv6geHpUxnb/G10anHdiA1Ywq88ZNx8D4XBoylTld8uimmyxdFPe+5PVUV5mmrpfSdk1R0vehDBYoJVwIYJjDRu5w2AUev1Y0hZ8=.AAOxNq3VWjS+4VQKcOeIoWQlYZjwPsRyuppDg+jMXgh/; SPC_U=15176250614; SPC_R_T_ID=YVOgOV6jXPm35klRubVuyoAPQUMz589C1GkfeuGSg0UE2yAjdvUhRdN0026r8tVWbVWqyK2Dwiw5fzMiFsbofM2A/jYcSYQGnJe3lZS/LTvXei72UjT9xErbLorCsgLVXEgPJIu4JRDGMsrftjDE1OBppsR4UIrh+bY4zQQQ8Rg=; SPC_R_T_IV=VUtjM2FvSldkS2N6cjRhSQ==; SPC_T_ID=YVOgOV6jXPm35klRubVuyoAPQUMz589C1GkfeuGSg0UE2yAjdvUhRdN0026r8tVWbVWqyK2Dwiw5fzMiFsbofM2A/jYcSYQGnJe3lZS/LTvXei72UjT9xErbLorCsgLVXEgPJIu4JRDGMsrftjDE1OBppsR4UIrh+bY4zQQQ8Rg=; SPC_T_IV=VUtjM2FvSldkS2N6cjRhSQ==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3011|SPC_F=G9Khewv1gbA8xLdTITEQoht1ijleW1zy; REC_T_ID=5f6cae35-9a56-11f1-b641-cef95df0155d; SPC_ST=fkJXM7c8m7bvKG6CFCGDFHhpo7lA/e+oBNbDz+TnOWzqZ65SFxISM8CcDykvWagpaIl6VEUtzoVozuhDKyNxmA/OtV3R0BiHDRvI2+MQXEy5Hq40Y7uEY20IR88I+kuH67bKFFugiMfLxG8GX3XMmXxRUn8ienSHzP5PMY4TwTZ0rGhbQlBlTzeYFVQPTpobAS6vRA/oxSSiw9iCgwIE8x+WnB16dFuS3jF4IdDkkBY=.AJ+wm2la6MvSruL/RemtOcNBt+GXFPbf5Vex4z4sAjNu; SPC_U=15670880472; SPC_R_T_ID=wGvc7WmwwGNmu2vYdvW1iMxZqDrKyq8d/uK1TEB6GUp0doSYK54VsOPUvdDvXFIW7gitFV1v8ZE1L3LyLGDJsrguasO8BfVWfMTwZe/FmxKXb+EWFsXHPfxkWmdZPhubUxGzPhEkVlgnOx7WbGUlIqIsbYMXqR2dhJkpsIqo8fo=; SPC_R_T_IV=SmRYTzJrUGd5YXpRaEZuSw==; SPC_T_ID=wGvc7WmwwGNmu2vYdvW1iMxZqDrKyq8d/uK1TEB6GUp0doSYK54VsOPUvdDvXFIW7gitFV1v8ZE1L3LyLGDJsrguasO8BfVWfMTwZe/FmxKXb+EWFsXHPfxkWmdZPhubUxGzPhEkVlgnOx7WbGUlIqIsbYMXqR2dhJkpsIqo8fo=; SPC_T_IV=SmRYTzJrUGd5YXpRaEZuSw==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3068|SPC_F=mSjkApnkR6s1vkV6Wp90GNYi1F2lTDf0; REC_T_ID=195f196e-9ca8-11f1-9afd-96462189ff78; SPC_ST=GXAkMZdvqCEB0w6HgBkGKH7a/h8zpBIpT4Sy5KUh8nvaV7bJCWZ6rIs7DcFotP6UdocUiFa42FG7PHKCyUwbvkm+cGlkkZN2dLHsNT+CiYVQJjXKXkOwa/al1R2n27Rd9WQlcdw3VqfaxrDEW+g08nCH4uD7QtZBNQ3h3lEjvnK4M1Soku/1HhkJ42iqy7iyvU0ngMoepv67KN08xRX00isLr/NNy2PQN8sVxIkJ1Vw=.AID1jFGMcH2yhdi5GDz/8VylGJGEAGjG8H+fiYkx4AtT; SPC_U=16443254028; SPC_R_T_ID=9XMt8g8XC5KA3b+rgG1tSU5V23705H6LbnmgQ+VEtB7ASHd5XEmYWqR6XVJdENg9iAWv7a5EDUlfcUfV7N7/ETwgizmoXIk0BrAw7yLSluhxgxDyuP4wyVj8fBCO2N0wMiqDB12jDmHGWhYQHHpqdiqCleNKmMUMpK9rd6ZUfQw=; SPC_R_T_IV=WVFTTDlRNUk3YUdqVVJJRg==; SPC_T_ID=9XMt8g8XC5KA3b+rgG1tSU5V23705H6LbnmgQ+VEtB7ASHd5XEmYWqR6XVJdENg9iAWv7a5EDUlfcUfV7N7/ETwgizmoXIk0BrAw7yLSluhxgxDyuP4wyVj8fBCO2N0wMiqDB12jDmHGWhYQHHpqdiqCleNKmMUMpK9rd6ZUfQw=; SPC_T_IV=WVFTTDlRNUk3YUdqVVJJRg==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3069|SPC_F=8YF3JeIeMRouotphqX4QkdCFSBvx3ubN; REC_T_ID=982585f5-9ca9-11f1-a97d-2203c2ecd967; SPC_ST=FIGJCaNXfZp6/k4/UgDwuhfWvWlJJQZhG+A3cRZZY73D4pY4Q2A8RwjxFm8Sri6iLUS10o1W9Y4KVTvHQr1nmkDYOGmSwUSgCylROJY8X6CsqgGGmtbmmaifIWZawtqCj+ID4gbCwpI3L8xtCruGedWjnRWu31CEJmHDuqBlvHW6YE+NZThhFpvgwqjdSmVPZ92vPIkgzFa7SRWEWGzKIpi3TuCVlH7Um1jrIx1+YGE=.AOc0cGmuYzznlqxlayDcqPaNWKFiWo5oxARFE9+SWysw; SPC_U=15478562819; SPC_R_T_ID=i1rUhhvg0OgzAH1zkMLdaverENohr1KilFPxu07cJEyeptCG6WOLat+elWk2zJ+GN3TXyrrnGSSh40ftGnn8Z+oZaxFpDbRuEsVlj7pAfs6DZX39GpBgmjttPuIiYnnMooX6yaDlYqSyZphz/VoLYPW5Qp1upVZIjsebASQEm78=; SPC_R_T_IV=MEk5NHJhZ296VWpGY2Ftdw==; SPC_T_ID=i1rUhhvg0OgzAH1zkMLdaverENohr1KilFPxu07cJEyeptCG6WOLat+elWk2zJ+GN3TXyrrnGSSh40ftGnn8Z+oZaxFpDbRuEsVlj7pAfs6DZX39GpBgmjttPuIiYnnMooX6yaDlYqSyZphz/VoLYPW5Qp1upVZIjsebASQEm78=; SPC_T_IV=MEk5NHJhZ296VWpGY2Ftdw==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064",
        "MY3070|SPC_F=sT1Rjgi1RgyY4E9SmhoSGjHdn3sPP5g9; REC_T_ID=9a60ed2b-9ca9-11f1-9e82-c69fd4e6801d; SPC_ST=6nHJsjuwGpcqDK23mVT7Ds3N6hflJfOlv9dsRRxvmsAtYf1hRTfyYOG4JFg1QXA7rg16lqFy6CmHTVpSYgoGsgkgvegBVE8NN4wDlBJXGb2JBRsTLNUrVT8IsrSpck09LZlyHdqFD8lo/bMwVlfPd0XhWCugwCC6Oqhn+dsX4eklfp0CrredvXpO7uke5BgtGd7OWQb/vddH4UcuvwTrf+wmBMHsk+vbHKbqmUVOcv8=.AEOZwfT6D4sktvdNaOd3FaJZ9P35IHipzRy1XgVQElmz; SPC_U=15387627787; SPC_R_T_ID=ffn/LyiJSEKnZpYDHJLunk3zFTD0mOEuOAFoyPUnkpT1S/DBr0gRRS4f01nAAgK99JtbpN3fCeBxJdqj44H9Hg38KiRiUqK5UwjLxk5uuxVDrUC4ZcffM3TqGNXFPo+aMSzhJzo4KVf5Mfp1FpMU36nVbAKEoqTS8XolnA3lKcI=; SPC_R_T_IV=b1BMdFhTeTZDdjJFbU9ERQ==; SPC_T_ID=ffn/LyiJSEKnZpYDHJLunk3zFTD0mOEuOAFoyPUnkpT1S/DBr0gRRS4f01nAAgK99JtbpN3fCeBxJdqj44H9Hg38KiRiUqK5UwjLxk5uuxVDrUC4ZcffM3TqGNXFPo+aMSzhJzo4KVf5Mfp1FpMU36nVbAKEoqTS8XolnA3lKcI=; SPC_T_IV=b1BMdFhTeTZDdjJFbU9ERQ==; language=vi; SPC_RNBV=6081003; shopee_app_version=29531; shopee_rn_bundle_version=6081003; shopee_rn_version=1763129064"
    )

    @Test
    fun testProcess11MyAccounts() {
        val client = OkHttpClient()

        val monthsToTest = listOf(
            "Tháng 8/2026 (01-08-2026 ~ 31-08-2026)" to Pair("01-08-2026 00:00:00", "31-08-2026 23:59:59"),
            "Tháng 9/2026 (01-09-2026 ~ 25-09-2026)" to Pair("01-09-2026 00:00:00", "25-09-2026 23:59:59")
        )

        val format = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("Asia/Kuala_Lumpur")

        for ((monthLabel, dateRange) in monthsToTest) {
            val startSec = format.parse(dateRange.first)!!.time / 1000L
            val endSec = format.parse(dateRange.second)!!.time / 1000L

            var totalCommRaw = 0L
            var totalOrders = 0
            var activeAccountsCount = 0

            println("==================================================")
            println("=== REPORT FOR MALAYSIA (MY) PERIOD: $monthLabel ===")
            println("==================================================")

            for (line in myLines) {
                val parts = line.split("|")
                val accName = parts[0].trim()
                val cookie = parts[1].trim()

                var accCommRaw = 0L
                var accOrders = 0
                var accTotalCount = 0

                var pageNum = 1
                val pageSize = 100

                do {
                    val url = "https://affiliate.shopee.com.my/api/v3/report/list?start_time=$startSec&end_time=$endSec&page_num=$pageNum&page_size=$pageSize"
                    val req = Request.Builder()
                        .url(url)
                        .addHeader("Cookie", cookie)
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36")
                        .get()
                        .build()

                    try {
                        val resp = client.newCall(req).execute()
                        val body = resp.body?.string() ?: ""

                        if (!resp.isSuccessful || !body.contains("\"code\":0")) {
                            if (pageNum == 1 && body.contains("\"code\":30002")) {
                                println(" Account '$accName' -> Cookie Expired (code: 30002)")
                            }
                            break
                        }

                        val root = JsonParser.parseString(body).asJsonObject
                        val data = root.getAsJsonObject("data")

                        if (pageNum == 1 && data.has("total_count")) {
                            accTotalCount = data.get("total_count").asInt
                        }

                        val list = if (data.has("list") && data.get("list").isJsonArray) data.getAsJsonArray("list") else null
                        if (list == null || list.size() == 0) break

                        for (elem in list) {
                            val item = elem.asJsonObject
                            val purchaseTime = item.get("purchase_time")?.asLong ?: 0L
                            val checkoutStatus = item.get("checkout_status")?.asString ?: ""

                            if (purchaseTime in startSec..endSec && checkoutStatus != "Invalid") {
                                val comm = item.get("estimated_total_commission_with_mcn")?.asLong
                                    ?: item.get("total_brand_commission")?.asLong
                                    ?: item.get("affiliate_net_commission")?.asString?.toLongOrNull()
                                    ?: item.get("estimated_total_commission")?.asLong
                                    ?: 0L

                                accCommRaw += comm
                                accOrders++
                            }
                        }

                        pageNum++
                    } catch (e: Exception) {
                        break
                    }
                } while ((pageNum - 1) * pageSize < accTotalCount && pageNum <= 15)

                if (accOrders > 0 || accCommRaw > 0) {
                    activeAccountsCount++
                    println(" Account '$accName' [MY] -> Orders: $accOrders | Comm: ${accCommRaw / 100000L} RM")
                }

                totalCommRaw += accCommRaw
                totalOrders += accOrders
            }

            println("==================================================")
            println("FINAL RESULT FOR MY [$monthLabel]:")
            println("Total Accounts Checked: ${myLines.size}")
            println("Accounts Active with Conversions: $activeAccountsCount")
            println("Total Valid Orders: $totalOrders")
            println("TOTAL COMMISSION (MYR): ${totalCommRaw / 100000L} RM")
            println("==================================================")
        }
    }
}
