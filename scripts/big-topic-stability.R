sym01 <- t(read.table("sym01.barplot", header=F))
sym11 <- t(read.table("sym11.barplot", header=F))

topic_names <- c("50 topics", "75 topics", "100 topics")

pdf("big-topic-stability.pdf", width=8)

par(mfrow=c(1,2))

barplot(sym01, main="AS prior", names.arg=topic_names, col="gray90")
barplot(sym11, main="SS prior", names.arg=topic_names, col="gray90")

dev.off()
