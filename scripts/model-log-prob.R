patents <- read.table("core.log_probs", header=F) / 103499
nyt <- read.table("nyt.log_probs", header=F) / 477465
news <- read.table("20news.log_probs", header=F) / 80012

offset <- (1:4 - 2.5) * 0.1

colors <- c('black','red','green','blue')

ylimit <- c(-.35, .35)

par(mfrow=c(3,1))
par(mar=c(2, 1, 3, 1))

plot(patents[,1], offset, col=colors, pch=19, yaxt="n", main="Patent abstracts", ylim=ylimit)

for (i in 1:4) {
  arrows(patents[i,1] - patents[i,2], offset[i], patents[i,1] + patents[i,2], offset[i], angle=90, code=3, length=0.02, col=colors[i])
}

plot(nyt[,1], offset, col=colors, pch=19, yaxt="n", xlim=c(-9.28, -9.22), main="NYT", ylim=ylimit)

for (i in 1:4) {
  arrows(nyt[i,1] - nyt[i,2], offset[i], nyt[i,1] + nyt[i,2], offset[i], angle=90, code=3, length=0.02, col=colors[i])
}

plot(news[,1], offset, col=colors, pch=19, yaxt="n", xlim=c(-8.33, -8.275), main="20News", ylim=ylimit)

for (i in 1:4) {
  arrows(news[i,1] - news[i,2], offset[i], news[i,1] + news[i,2], offset[i], angle=90, code=3, length=0.02, col=colors[i])
}
