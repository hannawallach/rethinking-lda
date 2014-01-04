hyperparams <- read.table("hyperparams-50.txt", header=F)

pdf("hyperparams-50.pdf")

par(mfrow=c(2,2))
par(mar=c(3,4,4,2) + 0.1)

hist(hyperparams[,1], main=expression(alpha), xlab="")
hist(hyperparams[,2], main=expression(paste(alpha, "'")), xlab="")
hist(hyperparams[,3], main=expression(beta), xlab="")
hist(log(hyperparams[,4]), main=expression(paste("log ", beta, "'")), xlab="")

dev.off()
