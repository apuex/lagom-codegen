package com.github.apuex.commerce.sales

trait ShardingEntityCommand extends Command {
  def entityId: String
}

