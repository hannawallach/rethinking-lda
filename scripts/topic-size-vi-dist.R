topic_size_vi <- read.table("topic-size-vi-dist.txt", header=F)

topics <- c(50, 75, 100)
colors <- c('black','red','green','blue')

plot(topics, topic_size_vi[1,2:4], type="l", ylim=c(3.8,6.4), xaxt="n", xlab="Topics", ylab="Variation of Information", main="Clustering distance from T=25", col=colors[1])

axis(side=1, at=topics)

for (i in 1:3) {
  arrows(topics[i], topic_size_vi[1,i+1] - topic_size_vi[1,i+4], topics[i], topic_size_vi[1,i+1] + topic_size_vi[1,i+4], angle=90, code=3, length=0.05, col=colors[1])
}

for (row in 2:4) {

  lines(topics, topic_size_vi[row,2:4], type="l", col=colors[row])

  for (i in 1:3) {
    arrows(topics[i], topic_size_vi[row,i+1] - topic_size_vi[row,i+4], topics[i], topic_size_vi[row,i+1] + topic_size_vi[row,i+4], angle=90, code=3, length=0.05, col=colors[row])
  }
}
