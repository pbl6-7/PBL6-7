import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, LogIn, UserPlus, Loader2 } from 'lucide-react';
import { login, register, getSecurityQuestions } from '@/api/user';
import type { LoginRequest, RegisterRequest, SecurityQuestion } from '@/types/user';

export default function AuthPage() {
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(true);
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  // 登录表单
  const [loginForm, setLoginForm] = useState<LoginRequest>({
    username: '',
    password: '',
  });

  // 注册表单
  const [registerForm, setRegisterForm] = useState<RegisterRequest>({
    username: '',
    password: '',
    realName: '',
    securityQuestionId: 0,
    securityAnswer: '',
  });

  // 密保问题列表
  const [securityQuestions, setSecurityQuestions] = useState<SecurityQuestion[]>([]);
  const [loadingQuestions, setLoadingQuestions] = useState(false);

  // 加载密保问题
  const loadSecurityQuestions = async () => {
    setLoadingQuestions(true);
    try {
      const res = await getSecurityQuestions();
      setSecurityQuestions(res.data.data);
      if (res.data.data.length > 0) {
        setRegisterForm(prev => ({ ...prev, securityQuestionId: res.data.data[0].questionId }));
      }
    } catch (err) {
      console.error('加载密保问题失败', err);
    } finally {
      setLoadingQuestions(false);
    }
  };

  // 切换到注册时加载密保问题
  const handleSwitchToRegister = () => {
    setIsLogin(false);
    setError('');
    if (securityQuestions.length === 0) {
      loadSecurityQuestions();
    }
  };

  // 处理登录
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const res = await login(loginForm);
      const { token, userId, username, role } = res.data.data;

      // 保存 token 和用户信息
      localStorage.setItem('token', token);
      localStorage.setItem('user', JSON.stringify({ userId, username, role }));

      // 跳转到首页
      navigate('/');
    } catch (err: any) {
      setError(err.message || '登录失败，请检查用户名和密码');
    } finally {
      setLoading(false);
    }
  };

  // 处理注册
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // 密码强度验证：至少8位，包含大小写字母、数字和特殊字符
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;
    if (!passwordRegex.test(registerForm.password)) {
      setError('密码强度不足，至少需要8位，包含大小写字母、数字和特殊字符');
      return;
    }

    if (!registerForm.securityQuestionId || !registerForm.securityAnswer) {
      setError('请填写密保问题和答案');
      return;
    }

    setLoading(true);

    try {
      await register(registerForm);
      alert('注册成功，请登录');
      setIsLogin(true);
      setLoginForm({ username: registerForm.username, password: '' });
    } catch (err: any) {
      // 优先使用后端返回的错误消息
      const errorMessage = err.response?.data?.message || err.message || '注册失败';
      setError(errorMessage);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 px-4">
      <div className="max-w-md w-full">
        {/* Logo */}
        <div className="text-center mb-8">
          <h1 className="text-4xl font-bold text-indigo-600 mb-2">校园活动平台</h1>
          <p className="text-gray-600">发现精彩活动，参与校园生活</p>
        </div>

        {/* 表单卡片 */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          {/* Tab 切换 */}
          <div className="flex mb-6 border-b border-gray-200">
            <button
              type="button"
              onClick={() => { setIsLogin(true); setError(''); }}
              className={`flex-1 pb-3 text-center font-medium transition-colors ${
                isLogin
                  ? 'text-indigo-600 border-b-2 border-indigo-600'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              登录
            </button>
            <button
              type="button"
              onClick={handleSwitchToRegister}
              className={`flex-1 pb-3 text-center font-medium transition-colors ${
                !isLogin
                  ? 'text-indigo-600 border-b-2 border-indigo-600'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              注册
            </button>
          </div>

          {/* 错误提示 */}
          {error && (
            <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
              {error}
            </div>
          )}

          {/* 登录表单 */}
          {isLogin ? (
            <form onSubmit={handleLogin} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  用户名
                </label>
                <input
                  type="text"
                  value={loginForm.username}
                  onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition"
                  placeholder="请输入用户名"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  密码
                </label>
                <div className="relative">
                  <input
                    type={showPassword ? 'text' : 'password'}
                    value={loginForm.password}
                    onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition pr-10"
                    placeholder="请输入密码"
                    required
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
              </div>

              <div className="flex items-center justify-between text-sm">
                <Link to="/forgot-password" className="text-indigo-600 hover:text-indigo-700">
                  忘记密码？
                </Link>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="animate-spin" size={18} />
                    登录中...
                  </>
                ) : (
                  <>
                    <LogIn size={18} />
                    登录
                  </>
                )}
              </button>
            </form>
          ) : (
            <form onSubmit={handleRegister} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  用户名
                </label>
                <input
                  type="text"
                  value={registerForm.username}
                  onChange={(e) => setRegisterForm({ ...registerForm, username: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition"
                  placeholder="请输入用户名"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  真实姓名
                </label>
                <input
                  type="text"
                  value={registerForm.realName}
                  onChange={(e) => setRegisterForm({ ...registerForm, realName: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition"
                  placeholder="请输入真实姓名"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  密码
                </label>
                <input
                  type="password"
                  value={registerForm.password}
                  onChange={(e) => setRegisterForm({ ...registerForm, password: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition"
                  placeholder="请输入密码（至少8位，包含大小写字母、数字和特殊字符）"
                  required
                  minLength={8}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  密保问题
                </label>
                <select
                  value={registerForm.securityQuestionId}
                  onChange={(e) => setRegisterForm({ ...registerForm, securityQuestionId: Number(e.target.value) })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition"
                  disabled={loadingQuestions}
                  required
                >
                  <option value={0}>请选择密保问题</option>
                  {securityQuestions.map((q) => (
                    <option key={q.questionId} value={q.questionId}>
                      {q.question}
                    </option>
                  ))}
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  密保答案
                </label>
                <input
                  type="text"
                  value={registerForm.securityAnswer}
                  onChange={(e) => setRegisterForm({ ...registerForm, securityAnswer: e.target.value })}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-indigo-500 focus:border-transparent outline-none transition"
                  placeholder="请输入密保答案"
                  required
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-indigo-600 text-white rounded-lg font-medium hover:bg-indigo-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="animate-spin" size={18} />
                    注册中...
                  </>
                ) : (
                  <>
                    <UserPlus size={18} />
                    注册
                  </>
                )}
              </button>
            </form>
          )}
        </div>

        {/* 底部链接 */}
      </div>
    </div>
  );
}
