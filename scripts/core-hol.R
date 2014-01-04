core_hol <- read.table("core-hol.txt", header=F)
core_hol_var <- read.table("core-hol-var.txt", header=F)

y_range=c(min(core_hol[,2:5]), max(core_hol[,2:5]))

plot(core_hol[,1], core_hol[,2], ylim=y_range, type='b', col="blue", ylab="Nats / token", xlab="Topics", main="Held-out probability", xaxt="n")

axis(side=1, at=c(25, 50, 75, 100))

for (i in 1:4) {
  arrows(core_hol[i,1], core_hol[i,2] - core_hol_var[i,2], core_hol[i,1], core_hol[i,2] + core_hol_var[i,2], angle=90, code=3, length=0.05, col="blue")
}

lines(core_hol[,1], core_hol[,3], type="b", col="green")

for (i in 1:4) {
  arrows(core_hol[i,1], core_hol[i,3] - core_hol_var[i,3], core_hol[i,1], core_hol[i,3] + core_hol_var[i,3], angle=90, code=3, length=0.05, col="green")
}

lines(core_hol[,1], core_hol[,4], type="b", col="red")

for (i in 1:4) {
  arrows(core_hol[i,1], core_hol[i,4] - core_hol_var[i,4], core_hol[i,1], core_hol[i,4] + core_hol_var[i,4], angle=90, code=3, length=0.05, col="red")
}

lines(core_hol[,1], core_hol[,5], type="b", col="black")

for (i in 1:4) {
  arrows(core_hol[i,1], core_hol[i,5] - core_hol_var[i,5], core_hol[i,1], core_hol[i,5] + core_hol_var[i,5], angle=90, code=3, length=0.05, col="black")
}
