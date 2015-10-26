<?php
namespace Home\Controller;
use Think\Controller;
class IndexController extends Controller {
    public function index(){
		$model = M('Article');
		// 取出总的记录数
		$count = $model->count();
		// 生成翻页对象
		$page = new \Think\Page($count, 10);
		// 生成翻页字符串
		$pageStr = $page->show();
		$this->assign('pageStr', $pageStr);
		// 取这一页的记录
		$data = $model->limit($page->firstRow, $page->listRows)->select();
		$result = json_encode($data);
		echo $result;exit;
    }
}