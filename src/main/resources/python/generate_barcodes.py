#-- coding:UTF-8 --
import os
import re
import sys
import barcode
from barcode.writer import ImageWriter



path = r"E:\学习资料\毕业设计-线上食品超市平台\食品溯源系统\traceNumber"  # 溯源条形码存放位置
if not os.path.exists(path):
    os.mkdir(path)

traceNumber = sys.argv[1]  # 溯源码

m = re.search(r'-(.+?)-', traceNumber)
if m:
   signature = m.group(1)


folder_path = os.path.join(path, signature)
if not os.path.exists(folder_path):
    os.mkdir(folder_path)

# 生成条形码
EAN = barcode.get_barcode_class("code128")
ean = EAN(str(traceNumber), writer=ImageWriter())
ean.default_writer_options['module_width'] = 0.21  # 设置条形码的模块宽度
ean.default_writer_options['module_height'] = 10  # 设置条形码的模块高度

# 保存
ean.save(os.path.join(folder_path, traceNumber))

