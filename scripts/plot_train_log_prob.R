# cat scriptname.R | R --slave --vanilla --args ...

results_dir_base = commandArgs()[5]

T = commandArgs()[6]
S = commandArgs()[7]

opt = commandArgs()[8]
path_assumption = commandArgs()[9]

num_runs = commandArgs()[10]

results_dir_a <- paste(results_dir_base, '/T', T, '-S', S, '-SYM', sep="");
results_dir_b <- paste('-OPT', opt, '-', path_assumption, '-ID', sep="");

pdf()

par(lwd=0.5) # set the line width

for (sym in c('00','01','10','11')) {

  results_dir_c <- paste(results_dir_a, sym, results_dir_b, sep="")

  for (id in 1:num_runs) {

    results_dir <- paste(results_dir_c, id, sep="")
    f <- paste(results_dir, '/log_prob.txt', sep="")
    data <- read.table(f, header=FALSE)

    if (id == 1) {
      all_data_model <- data$V1
    }
    else {
      all_data_model <- all_data_model + data$V1
    }
  }

  # all_data_model will contain the average for this model

  all_data_model <- all_data_model / as.numeric(num_runs)

  if (sym == "00") {
    all_data <- cbind(all_data_model)
  }
  else {
    all_data <- cbind(all_data, all_data_model)
  }
}

limits <- c(min(all_data), max(all_data))
colors <- c('black','red','green','blue')

xl <- 'Iteration'
yl <- 'Log Probability'

plot(all_data[,1], type="l", col=colors[1], ylim=limits, xlab=xl, ylab=yl);

for (sym in 2:4) {
  lines(all_data[,sym], col=colors[sym], ylim=limits);
}

dev.off()
